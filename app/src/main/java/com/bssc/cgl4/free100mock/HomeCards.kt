package com.bssc.cgl4.free100mock

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/** Independent Firebase-managed Home content. No app update needed to edit cards. */
data class HomeCard(
    val id:String,
    val sectionId:String,
    val sectionTitleEn:String,
    val sectionTitleHi:String,
    val sectionOrder:Int,
    val sortOrder:Int,
    val placement:String,
    val layout:String,
    val type:String,
    val theme:String,
    val iconEmoji:String,
    val iconName:String,
    val titleEn:String,
    val titleHi:String,
    val bodyEn:String,
    val bodyHi:String,
    val value:String,
    val badgeEn:String,
    val badgeHi:String,
    val buttonEn:String,
    val buttonHi:String,
    val imageUrl:String,
    val actionType:String,
    val actionValue:String,
    val claim:String,
    val sourceUrl:String,
    val startsAtMs:Long?,
    val endsAtMs:Long?
) {
    fun visibleAt(now:Long)= (startsAtMs==null || now>=startsAtMs) &&
        (endsAtMs==null || now<endsAtMs)
}

object HomeCardPolicy {
    const val MAX_VISIBLE=80
    val types=setOf("standard","metric","image","cutoff")
    val layouts=setOf("horizontal","stacked")
    val placements=setOf("top","middle","bottom")
    val themes=setOf("white","violet","blue","mint","gold","sunset")
    val routes=setOf("mock_list","insights","ranking","home","profile")
    val claims=setOf("expected","official")

    fun validCard(type:String,layout:String,placement:String,theme:String,
                  actionType:String,actionValue:String,claim:String,
                  sourceUrl:String,titleEn:String,titleHi:String,
                  imageUrl:String,value:String):Boolean {
        if(type !in types || layout !in layouts || placement !in placements ||
            theme !in themes || actionType !in setOf("none","internal","external")) return false
        if(actionType=="internal" && actionValue !in routes) return false
        if(actionType=="external" && !safeHttps(actionValue)) return false
        if(imageUrl.isNotBlank() && !safeHttps(imageUrl)) return false
        if(titleEn.isBlank() && titleHi.isBlank() && imageUrl.isBlank()) return false
        if(type=="image" && !safeHttps(imageUrl)) return false
        if(type=="cutoff" && (value.isBlank() || claim !in claims ||
            (claim=="official" &&
            (!safeHttps(sourceUrl) || !sourceUrl.startsWith("https://bssc.bihar.gov.in/"))))
            return false
        return sourceUrl.isBlank() || safeHttps(sourceUrl)
    }

    fun sorted(cards:List<HomeCard>)=cards.sortedWith(
        compareBy<HomeCard>({it.sectionOrder},{it.sectionId},{it.sortOrder},{it.id})
    ).take(MAX_VISIBLE)
}

/** Query only public active records. Firestore rules deny student writes.
 * Snapshot listeners are disposed when the root UI is destroyed.
 */
class HomeCardsRepository(private val context:Context) {
    fun observe(callback:(List<HomeCard>)->Unit):()->Unit {
        var disposed=false
        var registration:ListenerRegistration?=null
        CloudBackend.withIdentity(context,{ store:FirebaseFirestore ->
            if(!disposed) {
                registration=store.collection("home_cards")
                    .whereEqualTo("active",true).limit(100)
                    .addSnapshotListener {snapshot,error ->
                        if(!disposed) {
                            if(error!=null || snapshot==null) callback(emptyList())
                            else {
                                val parsed=snapshot.documents.mapNotNull{d ->
                                    try {
                                        fun field(key:String,length:Int=200)=
                                            d.getString(key).orEmpty().take(length).trim()
                                        val sectionId=field("sectionId",60).ifBlank{"updates"}
                                        val type=field("type",20).ifBlank{"standard"}
                                        val layout=field("layout",20).ifBlank{"horizontal"}
                                        val placement=field("placement",20).ifBlank{"middle"}
                                        val theme=field("theme",20).ifBlank{"white"}
                                        val actionType=field("actionType",20).ifBlank{"none"}
                                        val actionValue=field("actionValue",2048)
                                        val claim=field("claim",20)
                                        val sourceUrl=field("sourceUrl",2048)
                                        val titleEn=field("titleEn",100)
                                        val titleHi=field("titleHi",100)
                                        val imageUrl=field("imageUrl",2048)
                                        if(!HomeCardPolicy.validCard(type,layout,placement,theme,
                                            actionType,actionValue,claim,sourceUrl,
                                            titleEn,titleHi,imageUrl,field("value",40))) null
                                        else HomeCard(
                                            d.id,sectionId,field("sectionTitleEn",90),
                                            field("sectionTitleHi",90),
                                            (d.getLong("sectionOrder")?:50L).toInt().coerceIn(-1000,1000),
                                            (d.getLong("sortOrder")?:50L).toInt().coerceIn(-1000,1000),
                                            placement,layout,type,theme,
                                            field("iconEmoji",12),field("iconName",30),
                                            titleEn,titleHi,field("bodyEn",400),
                                            field("bodyHi",400),field("value",40),
                                            field("badgeEn",50),field("badgeHi",50),
                                            field("buttonEn",35),field("buttonHi",35),
                                            imageUrl,actionType,actionValue,claim,sourceUrl,
                                            d.getTimestamp("startsAt")?.toDate()?.time,
                                            d.getTimestamp("endsAt")?.toDate()?.time
                                        )
                                    } catch (_:Exception) {null}
                                }
                                callback(HomeCardPolicy.sorted(parsed))
                            }
                        }
                    }
            }
        },{if(!disposed)callback(emptyList())})
        return {disposed=true;registration?.remove()}
    }
}
