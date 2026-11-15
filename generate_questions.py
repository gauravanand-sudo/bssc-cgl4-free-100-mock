"""Original starter-question generator. Replace and editorial-review for production."""
from pathlib import Path
import random, json, math
ROOT=Path(__file__).parent/'app/src/main/assets'
# topic, question English, question Hindi, correct, distractors (separated by |)
GK_RAW='''
Bihar History;Who was the first President of India?;भारत के प्रथम राष्ट्रपति कौन थे?;Dr Rajendra Prasad;Jawaharlal Nehru|S Radhakrishnan|Zakir Husain
Bihar Geography;What is the capital of Bihar?;बिहार की राजधानी क्या है?;Patna;Gaya|Muzaffarpur|Bhagalpur
Bihar Geography;Which river flows through Patna?;पटना से होकर कौन-सी नदी बहती है?;Ganga;Yamuna|Narmada|Godavari
Bihar History;In which city was the ancient Nalanda Mahavihara located?;प्राचीन नालंदा महाविहार किस क्षेत्र में स्थित था?;Nalanda;Buxar|Arrah|Motihari
Bihar History;Who led the 1857 uprising at Jagdishpur?;जगदीशपुर में 1857 के विद्रोह का नेतृत्व किसने किया?;Kunwar Singh;Birsa Munda|Tatya Tope|Mangal Pandey
Bihar Geography;Which Indian state lies immediately south of Bihar?;बिहार के ठीक दक्षिण में कौन-सा भारतीय राज्य है?;Jharkhand;Uttar Pradesh|Sikkim|Assam
Bihar Geography;Which country borders Bihar to the north?;बिहार के उत्तर में किस देश की सीमा है?;Nepal;Bhutan|Bangladesh|Myanmar
Bihar History;Which dynasty is associated with Emperor Ashoka?;सम्राट अशोक किस राजवंश से संबंधित थे?;Maurya;Gupta|Pala|Chola
Bihar Geography;The Kosi is a tributary of which river?;कोसी किस नदी की सहायक नदी है?;Ganga;Yamuna|Mahanadi|Krishna
Bihar Geography;Bodh Gaya is associated with the enlightenment of whom?;बोधगया किसके ज्ञान प्राप्ति स्थल के रूप में प्रसिद्ध है?;Gautama Buddha;Mahavira|Guru Nanak|Kabir
Bihar History;Vaishali is particularly associated with which ancient republic?;वैशाली किस प्राचीन गणराज्य से जुड़ा था?;Vajji;Magadha|Kalinga|Avanti
Bihar Geography;Valmiki National Park is located in which state?;वाल्मीकि राष्ट्रीय उद्यान किस राज्य में है?;Bihar;Odisha|Kerala|Punjab
Bihar Culture;Chhath Puja is dedicated principally to which deity?;छठ पूजा मुख्यतः किस देवता को समर्पित है?;Sun God;Shiva|Indra|Agni
Bihar Culture;Madhubani painting originated in which cultural region?;मधुबनी चित्रकला किस सांस्कृतिक क्षेत्र से संबंधित है?;Mithila;Malwa|Marwar|Konkan
Bihar History;Which ancient university was located near present-day Bhagalpur?;वर्तमान भागलपुर के निकट कौन-सा प्राचीन विश्वविद्यालय था?;Vikramashila;Takshashila|Vallabhi|Odantapuri
Bihar Geography;Gaya is situated on the bank of which river?;गया किस नदी के तट पर स्थित है?;Falgu;Kosi|Gandak|Son
Bihar History;Who founded the Mauryan Empire?;मौर्य साम्राज्य की स्थापना किसने की?;Chandragupta Maurya;Ashoka|Bindusara|Harshavardhana
Bihar Geography;The Gandak River joins which major river in Bihar?;गंडक नदी बिहार में किस प्रमुख नदी से मिलती है?;Ganga;Yamuna|Damodar|Tapi
Bihar History;Which religion's 24th Tirthankara was Mahavira?;महावीर किस धर्म के 24वें तीर्थंकर थे?;Jainism;Buddhism|Sikhism|Hinduism
Bihar Culture;Sonepur Mela is traditionally associated with which confluence?;सोनपुर मेला परंपरागत रूप से किस संगम से जुड़ा है?;Ganga and Gandak;Ganga and Yamuna|Kosi and Kamla|Son and Punpun
Indian Polity;What is the minimum age to become President of India?;भारत का राष्ट्रपति बनने की न्यूनतम आयु क्या है?;35 years;25 years|30 years|40 years
Indian Polity;Which article abolishes untouchability?;कौन-सा अनुच्छेद अस्पृश्यता समाप्त करता है?;Article 17;Article 14|Article 19|Article 21
Indian Polity;Fundamental Rights are contained in which part of the Constitution?;संविधान के किस भाग में मौलिक अधिकार हैं?;Part III;Part II|Part IV|Part V
Indian Polity;Who appoints the Governor of an Indian state?;भारतीय राज्य के राज्यपाल की नियुक्ति कौन करता है?;President;Prime Minister|Chief Minister|Chief Justice
Indian Polity;How long is the normal term of the Lok Sabha?;लोकसभा का सामान्य कार्यकाल कितना होता है?;5 years;4 years|6 years|7 years
Indian Polity;Which house of Parliament is permanent?;संसद का कौन-सा सदन स्थायी है?;Rajya Sabha;Lok Sabha|Both houses|Neither house
Indian Polity;Which article of the Constitution guarantees equality before law?;विधि के समक्ष समानता कौन-सा अनुच्छेद सुनिश्चित करता है?;Article 14;Article 16|Article 25|Article 32
Indian Polity;Who is the ex officio Chairman of the Rajya Sabha?;राज्यसभा का पदेन सभापति कौन होता है?;Vice President;President|Prime Minister|Speaker
Indian Polity;The Directive Principles of State Policy are in which Part?;राज्य के नीति-निदेशक तत्व किस भाग में हैं?;Part IV;Part III|Part I|Part VI
Indian Polity;The Constitution of India came into effect on which date?;भारत का संविधान कब लागू हुआ?;26 January 1950;15 August 1947|26 November 1949|2 October 1950
Indian Polity;Who was the Chairman of the Constitution Drafting Committee?;संविधान प्रारूप समिति के अध्यक्ष कौन थे?;B R Ambedkar;Rajendra Prasad|Sardar Patel|Jawaharlal Nehru
Indian Polity;Which Constitutional Amendment lowered voting age from 21 to 18?;किस संशोधन ने मतदान आयु 21 से 18 वर्ष की?;61st Amendment;42nd Amendment|44th Amendment|73rd Amendment
Indian Polity;Panchayati Raj received constitutional status through which amendment?;पंचायती राज को संवैधानिक दर्जा किस संशोधन से मिला?;73rd Amendment;74th Amendment|52nd Amendment|86th Amendment
Indian Polity;Municipalities received constitutional status through which amendment?;नगरपालिकाओं को संवैधानिक दर्जा किस संशोधन से मिला?;74th Amendment;73rd Amendment|42nd Amendment|61st Amendment
Indian Polity;Which constitutional body conducts elections to Parliament?;संसद के चुनाव कौन-सा संवैधानिक निकाय आयोजित करता है?;Election Commission of India;UPSC|Finance Commission|NITI Aayog
Indian Polity;Who presides over the Lok Sabha?;लोकसभा की अध्यक्षता कौन करता है?;Speaker;Vice President|Governor|Attorney General
Indian History;In which year did India gain independence?;भारत को स्वतंत्रता किस वर्ष मिली?;1947;1942|1950|1935
Indian History;Who is known as the Iron Man of India?;भारत का लौह पुरुष किसे कहा जाता है?;Sardar Vallabhbhai Patel;Subhas Chandra Bose|Bhagat Singh|Mahatma Gandhi
Indian History;Where did the Dandi March end?;दांडी यात्रा कहाँ समाप्त हुई?;Dandi;Sabarmati|Champaran|Wardha
Indian History;Who wrote the book Discovery of India?;डिस्कवरी ऑफ इंडिया पुस्तक किसने लिखी?;Jawaharlal Nehru;Mahatma Gandhi|B R Ambedkar|Rabindranath Tagore
Indian History;In which year was the Quit India Movement launched?;भारत छोड़ो आंदोलन किस वर्ष शुरू हुआ?;1942;1920|1930|1947
Indian History;Who gave the slogan Do or Die during Quit India Movement?;भारत छोड़ो आंदोलन में करो या मरो का नारा किसने दिया?;Mahatma Gandhi;Subhas Chandra Bose|Lala Lajpat Rai|Tilak
Indian History;In which year did the Battle of Plassey take place?;प्लासी का युद्ध किस वर्ष हुआ?;1757;1764|1857|1799
Indian History;Who founded the Mughal Empire in India?;भारत में मुगल साम्राज्य की स्थापना किसने की?;Babur;Akbar|Humayun|Aurangzeb
Indian History;Which emperor built the Buland Darwaza at Fatehpur Sikri?;फतेहपुर सीकरी का बुलंद दरवाजा किसने बनवाया?;Akbar;Shah Jahan|Jahangir|Babur
Indian History;Who composed the national anthem of India?;भारत का राष्ट्रगान किसने रचा?;Rabindranath Tagore;Bankim Chandra Chatterjee|Sarojini Naidu|Ishwar Chandra Vidyasagar
Indian History;Which movement began with the Dandi March in 1930?;1930 की दांडी यात्रा से कौन-सा आंदोलन शुरू हुआ?;Civil Disobedience Movement;Non-Cooperation Movement|Quit India Movement|Swadeshi Movement
Indian History;Where was the Indian National Congress founded in 1885?;1885 में भारतीय राष्ट्रीय कांग्रेस की स्थापना कहाँ हुई?;Bombay (Mumbai);Calcutta (Kolkata)|Delhi|Madras (Chennai)
Indian Geography;Which is the longest river flowing entirely within India?;पूरी तरह भारत में बहने वाली सबसे लंबी नदी कौन-सी है?;Godavari;Ganga|Brahmaputra|Indus
Indian Geography;Which plateau occupies much of southern India?;दक्षिण भारत के बड़े भाग में कौन-सा पठार है?;Deccan Plateau;Tibetan Plateau|Malwa Plateau|Chota Nagpur Plateau
Indian Geography;Which sea lies west of India?;भारत के पश्चिम में कौन-सा समुद्र है?;Arabian Sea;Bay of Bengal|Red Sea|South China Sea
Indian Geography;What is the standard meridian of India?;भारत की मानक मध्याह्न रेखा क्या है?;82.5° E;75° E|90° E|68° E
Indian Geography;Which soil is especially suitable for cotton cultivation?;कपास की खेती के लिए कौन-सी मिट्टी विशेष उपयुक्त है?;Black soil;Laterite soil|Desert soil|Mountain soil
Indian Geography;Which mountain range separates the Deccan Plateau from the western coastal plain?;दक्कन पठार को पश्चिमी तटीय मैदान से कौन-सी पर्वतमाला अलग करती है?;Western Ghats;Aravalli|Vindhya|Himalaya
Indian Geography;Which river flows through the rift valley between Vindhya and Satpura?;विंध्य और सतपुड़ा के बीच भ्रंश घाटी से कौन-सी नदी बहती है?;Narmada;Krishna|Kaveri|Ganga
Indian Geography;Which is the southernmost point of mainland India?;मुख्य भूमि भारत का दक्षिणतम बिंदु कौन-सा है?;Kanyakumari;Indira Point|Rameswaram|Kochi
Indian Geography;Kaziranga National Park is in which state?;काजीरंगा राष्ट्रीय उद्यान किस राज्य में है?;Assam;Bihar|Kerala|Gujarat
Indian Geography;Sundarbans are famous for which vegetation?;सुंदरबन किस प्रकार की वनस्पति के लिए प्रसिद्ध है?;Mangrove forests;Alpine forests|Thorn scrub|Temperate conifers
Indian Geography;The Tropic of Cancer passes through how many Indian states?;कर्क रेखा भारत के कितने राज्यों से गुजरती है?;8;6|7|9
Economics;What does GDP stand for?;GDP का पूर्ण रूप क्या है?;Gross Domestic Product;General Domestic Price|Gross Development Product|Global Domestic Production
Economics;Which institution issues currency notes in India other than the one-rupee note?;भारत में एक रुपये के नोट के अलावा मुद्रा नोट कौन जारी करता है?;Reserve Bank of India;SEBI|NABARD|State Bank of India
Economics;Inflation means a general rise in what?;मुद्रास्फीति का अर्थ सामान्यतः किसमें वृद्धि है?;Prices;Employment|Exports|Population
Economics;What is the full form of GST?;GST का पूर्ण रूप क्या है?;Goods and Services Tax;General Sales Tariff|Government Service Tax|Goods Supply Tariff
Economics;Which Indian institution manages monetary policy?;भारत में मौद्रिक नीति का संचालन कौन-सा संस्थान करता है?;Reserve Bank of India;Election Commission|CAG|UPSC
Economics;What is the name of India's central bank?;भारत के केंद्रीय बैंक का नाम क्या है?;Reserve Bank of India;State Bank of India|Punjab National Bank|NABARD
Economics;In which sector is farming classified?;कृषि को अर्थव्यवस्था के किस क्षेत्र में वर्गीकृत किया जाता है?;Primary sector;Secondary sector|Tertiary sector|Quaternary sector
Economics;What do we call a tax levied on an individual's income?;व्यक्ति की आय पर लगने वाले कर को क्या कहते हैं?;Income tax;Excise duty|Customs duty|Property tax
Economics;Which body replaced the Planning Commission of India?;भारत के योजना आयोग का स्थान किस निकाय ने लिया?;NITI Aayog;Finance Commission|RBI|SEBI
General Awareness;What is the national animal of India?;भारत का राष्ट्रीय पशु कौन-सा है?;Bengal Tiger;Asian Lion|Elephant|Leopard
General Awareness;What is the national bird of India?;भारत का राष्ट्रीय पक्षी कौन-सा है?;Indian Peacock;Sparrow|Parrot|Eagle
General Awareness;Which is the smallest prime number?;सबसे छोटी अभाज्य संख्या कौन-सी है?;2;1|3|0
General Awareness;Which planet is known as the Red Planet?;लाल ग्रह किसे कहते हैं?;Mars;Venus|Jupiter|Mercury
General Awareness;Which instrument measures atmospheric pressure?;वायुमंडलीय दबाव किस यंत्र से मापा जाता है?;Barometer;Thermometer|Hygrometer|Anemometer
General Awareness;Which day is celebrated as Constitution Day in India?;भारत में संविधान दिवस कब मनाया जाता है?;26 November;15 August|26 January|2 October
General Awareness;Where is the headquarters of the United Nations?;संयुक्त राष्ट्र का मुख्यालय कहाँ है?;New York;Geneva|Paris|London
General Awareness;Which gas is most abundant in Earth's atmosphere?;पृथ्वी के वायुमंडल में सर्वाधिक कौन-सी गैस है?;Nitrogen;Oxygen|Carbon dioxide|Hydrogen
General Awareness;Which vitamin is produced in the skin with sunlight exposure?;सूर्य के प्रकाश से त्वचा में कौन-सा विटामिन बनता है?;Vitamin D;Vitamin A|Vitamin B12|Vitamin K
General Awareness;What is the chemical symbol of gold?;सोने का रासायनिक संकेत क्या है?;Au;Ag|Fe|Cu
'''.strip()
SCI_RAW='''
Physics;What is the SI unit of force?;बल की SI इकाई क्या है?;Newton;Joule|Watt|Pascal
Physics;What is the SI unit of electrical resistance?;विद्युत प्रतिरोध की SI इकाई क्या है?;Ohm;Volt|Ampere|Watt
Physics;What is the approximate speed of light in vacuum?;निर्वात में प्रकाश की लगभग गति कितनी है?;3 × 10^8 m/s;3 × 10^6 m/s|3 × 10^5 m/s|3 × 10^3 m/s
Physics;Which lens is used to correct myopia?;निकट दृष्टि दोष ठीक करने के लिए कौन-सा लेंस उपयोग होता है?;Concave lens;Convex lens|Cylindrical mirror|Plane lens
Physics;Sound cannot travel through which medium?;ध्वनि किस माध्यम से नहीं गुजर सकती?;Vacuum;Water|Air|Steel
Physics;What is the SI unit of electric current?;विद्युत धारा की SI इकाई क्या है?;Ampere;Ohm|Volt|Joule
Physics;Which color of visible light has the longest wavelength?;दृश्य प्रकाश में सबसे अधिक तरंगदैर्ध्य किस रंग का होता है?;Red;Violet|Blue|Green
Physics;What does a voltmeter measure?;वोल्टमीटर क्या मापता है?;Potential difference;Electric current|Resistance|Frequency
Physics;Which form of energy is stored in a stretched spring?;तनी हुई स्प्रिंग में किस प्रकार की ऊर्जा संग्रहीत रहती है?;Elastic potential energy;Sound energy|Nuclear energy|Chemical energy
Physics;Which law relates voltage current and resistance?;वोल्टेज, धारा और प्रतिरोध का संबंध कौन-सा नियम बताता है?;Ohm's law;Newton's law|Boyle's law|Hooke's law
Chemistry;What is the chemical formula of water?;पानी का रासायनिक सूत्र क्या है?;H2O;CO2|O2|H2O2
Chemistry;What is the pH of a neutral solution at 25°C?;25°C पर उदासीन विलयन का pH कितना होता है?;7;3|9|12
Chemistry;Which element has atomic number 1?;किस तत्व का परमाणु क्रमांक 1 है?;Hydrogen;Helium|Oxygen|Carbon
Chemistry;Which gas is released when an acid reacts with a carbonate?;अम्ल और कार्बोनेट की क्रिया से कौन-सी गैस निकलती है?;Carbon dioxide;Oxygen|Hydrogen|Nitrogen
Chemistry;What is the chemical symbol of sodium?;सोडियम का रासायनिक संकेत क्या है?;Na;So|S|Sn
Chemistry;Which metal is liquid at room temperature?;कमरे के तापमान पर कौन-सी धातु द्रव है?;Mercury;Copper|Iron|Aluminium
Chemistry;Rusting of iron requires oxygen and what else?;लोहे में जंग लगने के लिए ऑक्सीजन के साथ और क्या चाहिए?;Moisture;Nitrogen|Carbon monoxide|Helium
Chemistry;Which gas do plants absorb during photosynthesis?;प्रकाश संश्लेषण के समय पौधे कौन-सी गैस लेते हैं?;Carbon dioxide;Oxygen|Nitrogen|Hydrogen
Chemistry;What is the common name for sodium chloride?;सोडियम क्लोराइड का सामान्य नाम क्या है?;Common salt;Baking soda|Washing soda|Bleaching powder
Chemistry;Which acid is present in lemon?;नींबू में कौन-सा अम्ल पाया जाता है?;Citric acid;Sulfuric acid|Hydrochloric acid|Nitric acid
Biology;Which organ pumps blood through the human body?;मनुष्य के शरीर में रक्त कौन-सा अंग पंप करता है?;Heart;Liver|Kidney|Lung
Biology;Which organelle is known as the powerhouse of the cell?;कोशिका का ऊर्जा गृह किस अंगक को कहते हैं?;Mitochondria;Ribosome|Nucleus|Golgi apparatus
Biology;What is the basic structural unit of life?;जीवन की मूल संरचनात्मक इकाई क्या है?;Cell;Tissue|Organ|Molecule
Biology;Which blood cells help fight infection?;कौन-सी रक्त कोशिकाएँ संक्रमण से रक्षा करती हैं?;White blood cells;Red blood cells|Platelets|Plasma
Biology;Which vitamin deficiency causes scurvy?;किस विटामिन की कमी से स्कर्वी होता है?;Vitamin C;Vitamin A|Vitamin D|Vitamin E
Biology;Which organ filters blood and produces urine?;कौन-सा अंग रक्त छानकर मूत्र बनाता है?;Kidneys;Heart|Pancreas|Stomach
Biology;What is the largest organ of the human body?;मनुष्य का सबसे बड़ा अंग कौन-सा है?;Skin;Liver|Heart|Brain
Biology;Which pigment gives leaves their green color?;पत्तियों को हरा रंग कौन-सा वर्णक देता है?;Chlorophyll;Hemoglobin|Melanin|Keratin
Biology;Which hormone helps regulate blood glucose?;रक्त ग्लूकोज के नियंत्रण में कौन-सा हार्मोन मदद करता है?;Insulin;Thyroxine|Adrenaline|Melatonin
Biology;Which part of a plant absorbs most water from soil?;पौधे का कौन-सा भाग मिट्टी से अधिकांश जल सोखता है?;Roots;Leaves|Flowers|Fruit
Biology;How many chambers are present in the human heart?;मनुष्य के हृदय में कितने कक्ष होते हैं?;4;2|3|5
Biology;Which type of blood vessel carries blood away from the heart?;हृदय से रक्त दूर ले जाने वाली रक्तवाहिका कौन-सी है?;Artery;Vein|Capillary|Lymph vessel
Biology;Which process produces gametes with half the chromosome number?;किस प्रक्रिया से आधे गुणसूत्र वाले युग्मक बनते हैं?;Meiosis;Mitosis|Budding|Binary fission
Biology;Which nutrient is primarily used for building and repairing body tissues?;शरीर के ऊतकों के निर्माण और मरम्मत के लिए मुख्य पोषक तत्व क्या है?;Protein;Fat|Water|Mineral salt
Biology;Which microorganism is responsible for malaria?;मलेरिया किस सूक्ष्मजीव के कारण होता है?;Plasmodium;Vibrio cholerae|Salmonella|Rhizobium
Environment;Which atmospheric layer contains most ozone that absorbs UV radiation?;पराबैंगनी किरणें सोखने वाली अधिकांश ओजोन किस परत में होती है?;Stratosphere;Troposphere|Mesosphere|Thermosphere
Environment;Which gas is a major contributor to anthropogenic global warming?;मानवजनित वैश्विक तापन में प्रमुख योगदान देने वाली गैस कौन-सी है?;Carbon dioxide;Argon|Helium|Neon
Environment;Which of these is a renewable energy source?;निम्न में से कौन-सा नवीकरणीय ऊर्जा स्रोत है?;Solar energy;Coal|Petroleum|Natural gas
Environment;Which trophic level includes green plants in a food chain?;खाद्य शृंखला में हरे पौधे किस पोषी स्तर पर होते हैं?;Producers;Primary consumers|Secondary consumers|Decomposers
Environment;Which process converts atmospheric nitrogen into useful compounds?;कौन-सी प्रक्रिया वायुमंडलीय नाइट्रोजन को उपयोगी यौगिकों में बदलती है?;Nitrogen fixation;Respiration|Transpiration|Sublimation
'''.strip()

def parse(raw, section):
    out=[]
    for line in raw.splitlines():
        cols=line.strip().split(';')
        assert len(cols)==5,cols
        topic,en,hi,good,others=cols
        wrong=others.split('|')
        assert len(wrong)==3 and len(set([good]+wrong))==4,(en,wrong)
        out.append(dict(section=section,topic=topic,en=en,hi=hi,good=good,wrong=wrong))
    return out
GK=parse(GK_RAW,'General Studies')
SCI=parse(SCI_RAW,'Science & Maths')
assert len(GK)>=50 and len(SCI)>=25

def build_question(section,topic,en,hi,correct,wrong,ex_en=None,ex_hi=None,seed=None):
    rng=random.Random(seed)
    opts=[str(correct)]+[str(x) for x in wrong]
    # ensure distractors distinct and avoid equal correct
    assert len(set(opts))==4,(en,opts)
    rng.shuffle(opts)
    idx=opts.index(str(correct))
    return dict(section=section,topic=topic,questionEn=en,questionHi=hi,optionsEn=opts,optionsHi=opts,
                correctIndex=idx,explanationEn=ex_en or f'The correct answer is {correct}.',
                explanationHi=ex_hi or f'सही उत्तर: {correct}।')

def four_num(val, delta=2):
    cands=[val-delta,val+delta,val+2*delta,val-2*delta,val+3*delta]
    return [x for x in cands if x!=val][:3]

def math_q(rng):
    kind=rng.randrange(10)
    if kind==0:
        n=rng.randrange(10,80); percent=rng.choice([10,15,20,25,30,40,50]); val=n*percent//100 if n*percent%100==0 else None
        if val is None: n=rng.randrange(1,8)*20; val=n*percent//100
        return ('Percentages',f'What is {percent}% of {n}?',f'{n} का {percent}% कितना है?',val,four_num(val,2))
    if kind==1:
        a=rng.randrange(12,50); b=rng.randrange(12,50); ans=(a+b)//2; b=2*ans-a
        return ('Averages',f'Find the average of {a} and {b}.',f'{a} और {b} का औसत ज्ञात करें।',ans,four_num(ans,3))
    if kind==2:
        p=rng.choice([1000,2000,3000,4000,5000]); r=rng.choice([5,6,8,10]); t=rng.choice([2,3,4]); ans=p*r*t//100
        return ('Simple Interest',f'Find simple interest on ₹{p} at {r}% per annum for {t} years.',f'₹{p} पर {r}% वार्षिक दर से {t} वर्ष का साधारण ब्याज ज्ञात करें।',ans,four_num(ans,50))
    if kind==3:
        x=rng.randrange(12,46); y=rng.randrange(5,12); ans=x+y
        return ('Arithmetic',f'Solve: {x} + {y} × 1 = ?',f'हल करें: {x} + {y} × 1 = ?',ans,four_num(ans,2))
    if kind==4:
        a=rng.randrange(4,15);b=rng.randrange(2,10);ans=a*b
        return ('Arithmetic',f'What is {a} multiplied by {b}?',f'{a} और {b} का गुणनफल क्या है?',ans,four_num(ans,5))
    if kind==5:
        a=rng.randrange(4,20); b=rng.randrange(2,9); ans=a*b
        return ('Time & Work',f'A worker completes {a} units per day. How many units in {b} days?',f'मजदूर प्रतिदिन {a} इकाई काम करता है। {b} दिनों में कितना काम करेगा?',ans,four_num(ans,a))
    if kind==6:
        length=rng.randrange(7,27);width=rng.randrange(5,17);ans=2*(length+width)
        return ('Mensuration',f'Find perimeter of a rectangle {length} cm long and {width} cm wide.',f'{length} सेमी लंबाई और {width} सेमी चौड़ाई वाले आयत का परिमाप ज्ञात करें।',ans,four_num(ans,4))
    if kind==7:
        speed=rng.randrange(30,81,10);hours=rng.randrange(2,6);ans=speed*hours
        return ('Speed & Distance',f'A vehicle moves at {speed} km/h for {hours} hours. Distance?',f'वाहन {speed} किमी/घंटा की गति से {hours} घंटे चलता है। दूरी?',ans,four_num(ans,20))
    if kind==8:
        original=rng.randrange(8,40)*10; discount=rng.choice([10,20,30,40,50]);ans=original*(100-discount)//100
        return ('Profit & Loss',f'An item costs ₹{original} and gets {discount}% discount. Sale price?',f'₹{original} की वस्तु पर {discount}% छूट है। विक्रय मूल्य?',ans,four_num(ans,10))
    x=rng.randrange(2,20);a=rng.randrange(2,8);b=rng.randrange(2,40);result=a*x+b
    return ('Algebra',f'If {a}x + {b} = {result}, find x.',f'यदि {a}x + {b} = {result}, तो x ज्ञात करें।',x,four_num(x,2))

def reason_q(rng):
    kind=rng.randrange(10)
    if kind==0:
        start=rng.randrange(2,40);delta=rng.randrange(2,12);series=[start+i*delta for i in range(5)];val=series[-1]+delta
        return ('Number Series',f'Find the next number: {", ".join(map(str,series))}, ?',f'अगली संख्या: {", ".join(map(str,series))}, ?',val,four_num(val,delta+1))
    if kind==1:
        a=rng.randrange(1,10);b=rng.randrange(2,5);seq=[a*b**i for i in range(4)];val=a*b**4
        return ('Number Series',f'Find the next number: {", ".join(map(str,seq))}, ?',f'अगली संख्या ज्ञात करें: {", ".join(map(str,seq))}, ?',val,[val//b,val+b,val+b*2])
    if kind==2:
        n=rng.randrange(5,22);answer=n*n
        return ('Number Logic',f'What is the square of {n}?',f'{n} का वर्ग क्या है?',answer,[answer-2*n+1,answer+n,answer+2*n+1])
    if kind==3:
        letter=rng.choice(list('ABCDEFGHJKLMN')); shift=rng.randrange(2,7);answer=chr(ord(letter)+shift)
        return ('Alphabet',f'In an alphabet code each letter moves {shift} positions forward. {letter} becomes?',f'कूट में प्रत्येक अक्षर {shift} स्थान आगे जाता है। {letter} क्या बनेगा?',answer,[chr(ord(letter)+shift-1),chr(ord(letter)+shift+1),chr(ord(letter)+shift+2)])
    if kind==4:
        day=rng.randrange(7);n=rng.randrange(8,40);days=['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday']; days_hi=['सोमवार','मंगलवार','बुधवार','गुरुवार','शुक्रवार','शनिवार','रविवार']; ans=days[(day+n)%7]
        return ('Calendar',f'If today is {days[day]}, what day will it be after {n} days?',f'यदि आज {days_hi[day]} है, तो {n} दिनों बाद कौन-सा दिन होगा?',ans,[x for x in days if x!=ans][:3])
    if kind==5:
        east=rng.randrange(3,12); north=rng.randrange(3,12);ans=f'{east} m East and {north} m North'
        return ('Directions',f'Ravi walks {east} m east, then {north} m north. Where is he from the start?',f'रवि {east} मीटर पूर्व, फिर {north} मीटर उत्तर चलता है। आरंभ से उसकी स्थिति?',ans,[f'{east} m West and {north} m North', f'{east} m East and {north} m South',f'{north} m West and {east} m South'])
    if kind==6:
        n=rng.randrange(2,13); k=rng.randrange(3,10);val=n*k
        return ('Analogy',f'{n} : {n*2} :: {val} : ?',f'{n} : {n*2} :: {val} : ?',val*2,[val*2+2,val*2-2,val*2+k])
    if kind==7:
        x=rng.randrange(10,40);y=rng.randrange(x+1,x+25);z=rng.randrange(y+1,y+25)
        return ('Comparisons',f'If A = {x}, B = {y}, and C = {z}, which is greatest?',f'यदि A = {x}, B = {y}, C = {z}, तो सबसे बड़ा कौन है?', 'C',['A','B','All equal'])
    if kind==8:
        x=rng.randrange(2,10); y=rng.randrange(2,10); z=rng.randrange(2,10);val=x+y+z
        return ('Number Logic',f'What is the sum of digits {x}, {y}, and {z}?',f'अंकों {x}, {y}, {z} का योग क्या है?',val,four_num(val,2))
    word=rng.choice(['CAT','DOG','BAG','SUN','MAP','BAT','HEN','TOP']);value=sum(ord(c)-64 for c in word)
    return ('Coding-Decoding',f'If A=1, B=2, ... Z=26, find the code-sum of {word}.',f'यदि A=1, B=2, ... Z=26, तो {word} के अक्षरों का योग क्या है?',value,four_num(value,3))

for test in range(1,5):
    rng=random.Random(44200+test*997)
    questions=[]
    for row in rng.sample(GK,50):
        questions.append(build_question('General Studies',row['topic'],row['en'],row['hi'],row['good'],row['wrong'],seed=rng.random()))
    for row in rng.sample(SCI,25):
        questions.append(build_question('Science & Maths',row['topic'],row['en'],row['hi'],row['good'],row['wrong'],seed=rng.random()))
    for _ in range(25):
        topic,en,hi,c,w=math_q(rng)
        questions.append(build_question('Science & Maths',topic,en,hi,c,w,seed=rng.random()))
    for _ in range(50):
        topic,en,hi,c,w=reason_q(rng)
        questions.append(build_question('Reasoning',topic,en,hi,c,w,seed=rng.random()))
    rng.shuffle(questions)
    for i,q in enumerate(questions): q['id']=f'M{test}-{i+1:03d}'
    assert len(questions)==150
    assert len(set(q['id'] for q in questions))==150
    assert all(len(set(q['optionsEn']))==4 and 0<=q['correctIndex']<4 for q in questions)
    (ROOT/f'mock_{test:02d}.json').write_text(json.dumps(questions,ensure_ascii=False,indent=2),encoding='utf8')
print('Generated 4 exams x 150 questions. Curated GS:',len(GK),'Science:',len(SCI))
