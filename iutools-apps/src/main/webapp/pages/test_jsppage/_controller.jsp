<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<script charset="UTF-8">
	// This function will be invoked when you press the [Try Code] button
    function tryCode() {
        var weController = new WordEntryController({});
        weController.show();
        weController.displayWordBeingLookedUp('aanniaviliaqtut');
        weController.displayWordEntry(resp);
    }

    var resp = JSON.parse(
      "{\n" +
        "    \"errorMessage\": null,\n" +
        "    \"failingInputs\": null,\n" +
        "    \"lang\": \"iu\",\n" +
        "    \"matchingWords\": [\n" +
        "        \"aanniaviliaqtutik\",\n" +
        "        \"aanniaviliaqtut\"\n" +
        "    ],\n" +
        "    \"otherLang\": \"en\",\n" +
        "    \"queryWordEntry\": {\n" +
        "        \"definition\": null,\n" +
        "        \"examplesForOrigWordTranslation\": {\n" +
        "            \"ALL\": [\n" +
        "                [\n" +
        "                    \"takuluarumaqattanginnama <strong>aanniaviliaqtutik</strong> aullaqtunik.\",\n" +
        "                    \"We do not like to see too many people go on <strong>medical travel</strong>.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"attanangittumiittiariaqaraluarmata nunavummi aullaraimmata <strong>aanniaviliaqtutik</strong>.\",\n" +
        "                    \"Our constituents are entitled to a safe and welcoming environment when they travel out of Nunavut for <strong>medical care</strong>.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"taimailingasuunguvut <strong>aanniaviliaqtutik</strong> aullaqtut.\",\n" +
        "                    \"That is not what happens with the <strong>medical patients</strong>.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"uqallaqatigikatalauqtutigut taikunngaqattaqtut <strong>aanniaviliaqtutik</strong>, taikani tujurmijaqtuqattaqtut.\",\n" +
        "                    \"We’ve talked to <strong>clients</strong>, patients who have gone through the facility.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"qujannamiik nalunaiqsigavit piliriarijassinnit <strong>aanniaviliaqtutik</strong> aullaqattaqtunut.\",\n" +
        "                    \"Thanks for that explanation of the work that you’re doing on <strong>medical travel</strong>.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"qikiqtaalummi 15-ngujut nunaliit iqalunnuuqattaqpuq <strong>aanniaviliaqtutik</strong>.\",\n" +
        "                    \"For the Baffin region there are fifteen of us that come to Iqaluit to the <strong>regional hospital</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"clients\": [\n" +
        "                [\n" +
        "                    \"uqallaqatigikatalauqtutigut taikunngaqattaqtut <strong>aanniaviliaqtutik</strong>, taikani tujurmijaqtuqattaqtut.\",\n" +
        "                    \"We’ve talked to <strong>clients</strong>, patients who have gone through the facility.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"medical care\": [\n" +
        "                [\n" +
        "                    \"attanangittumiittiariaqaraluarmata nunavummi aullaraimmata <strong>aanniaviliaqtutik</strong>.\",\n" +
        "                    \"Our constituents are entitled to a safe and welcoming environment when they travel out of Nunavut for <strong>medical care</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"medical patients\": [\n" +
        "                [\n" +
        "                    \"taimailingasuunguvut <strong>aanniaviliaqtutik</strong> aullaqtut.\",\n" +
        "                    \"That is not what happens with the <strong>medical patients</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"medical travel\": [\n" +
        "                [\n" +
        "                    \"takuluarumaqattanginnama <strong>aanniaviliaqtutik</strong> aullaqtunik.\",\n" +
        "                    \"We do not like to see too many people go on <strong>medical travel</strong>.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"qujannamiik nalunaiqsigavit piliriarijassinnit <strong>aanniaviliaqtutik</strong> aullaqattaqtunut.\",\n" +
        "                    \"Thanks for that explanation of the work that you’re doing on <strong>medical travel</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"regional hospital\": [\n" +
        "                [\n" +
        "                    \"qikiqtaalummi 15-ngujut nunaliit iqalunnuuqattaqpuq <strong>aanniaviliaqtutik</strong>.\",\n" +
        "                    \"For the Baffin region there are fifteen of us that come to Iqaluit to the <strong>regional hospital</strong>.\"\n" +
        "                ]\n" +
        "            ]\n" +
        "        },\n" +
        "        \"examplesForRelWordsTranslation\": {\n" +
        "            \"boarding\": [\n" +
        "                [\n" +
        "                    \"ullutuinnautillugu, qitiqquummiilaak qamigujjaaluugatta, apiqqutiqalauqtunga tujurmiviuvattunik <strong>aanniaviliaqpattunut</strong>.\",\n" +
        "                    \"On Friday, or sorry, Thursday, going back in time when we were in the dark, right? The question I was asking was in regard to the <strong>boarding</strong> home.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"pulaarialauqsimaliqpiit <strong>aanniaviliaqpattunut</strong> tujurmivinnut uqaqatiqariaqturlutit isumaaluutiuvattunik amma qanga kingulliqpaami pularalauqsimava minista liina piitusan <strong>aanniaviliaqpattunut</strong> tujurmivimmut.\",\n" +
        "                    \"Have you ever visited these <strong>boarding</strong> homes to discuss these concerns that are arising and when was the last time the minister visited the Lena Pederson Boarding Home.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"boarding ... clients ... medical travel\": [\n" +
        "                [\n" +
        "                    \"taakkua <strong>aanniaviliaqsimajut</strong> tujurmivimmiisuungummata, angirrarijaujunilu.\",\n" +
        "                    \"They fund the <strong>boarding</strong> home stays, commercial accommodations, and private billets <strong>for clients on medical travel</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"client\": [\n" +
        "                [\n" +
        "                    \"tavvasainnarlu piuniqpaakkut <strong>aanniaviliaqsimajut</strong> ikajurasuttavut kamagigiaqaqpattutigu.\",\n" +
        "                    \"It’s a lose-lose scenario if you’re sitting in my seat, but at the same time we have to do what is best for the <strong>client</strong>. That is the focus of not just the review, but the focus of the department.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"medical\": [\n" +
        "                [\n" +
        "                    \"Haviujaq: ikajuqtauvinnut pilirianit titiraqsimammat qangattautinut <strong>aanniaviliaqpattunut</strong>.\",\n" +
        "                    \"Mr. Havioyak: On the treatment programs there is a line there that says <strong>medical</strong> travel.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"atausiinnarmitqangatasuulirijimit kaanturaaliuqtailigissi <strong>aanniaviliaqtunut</strong> aullatittiqattarnirmut.\",\n" +
        "                    \"Please do not give one airline the contract for <strong>medical</strong> transportation.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"medical appointments\": [\n" +
        "                [\n" +
        "                    \"taanna tatalualiqattallarilirngat tammaatirvik ilangigguu inissaqanngittialiqattarngata namiivvissaqarunniiqtutillu <strong>aanniaviliaqsimajut</strong>.\",\n" +
        "                    \"Some patients cannot be accommodated, without any space available, although they are here to attend to <strong>medical appointments</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"medical reasons\": [\n" +
        "                [\n" +
        "                    \"innatuqarnik aullaqtuqalaurmat aatuvaamut <strong>aanniaviliaqtunik</strong>.\",\n" +
        "                    \"There were some elderly patients who travelled to Ottawa for <strong>medical reasons</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"medical travel\": [\n" +
        "                [\n" +
        "                    \"pitjutiqattuq <strong>aanniaviliaqpattunut</strong> ingirrajjutingita kiinaujaqturutinginnut.\",\n" +
        "                    \"It’s concerning <strong>medical travel</strong> costs.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"apiqqutigijarali pijjutiqarniqsauvuq qallunaat nunangannut <strong>aanniaviliaqpattunut</strong> iqalunnuunngittuq.\",\n" +
        "                    \"The question that I am asking is more to do with southern <strong>medical travel</strong> and not to Iqaluit.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"kiujunnaqtungali taakkua aanniaqaqtaililirijikkut qautamaaqsiutinginnit <strong>aanniaviliaqpattunut</strong> kamajiuvattut iqqanaijaqtiqaqtuta.\",\n" +
        "                    \"I can respond by saying that the Department of Health deals with the daily operations of <strong>medical travel</strong> with dedicated staff.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"Haviujaq: nalunaqsijunnaqpit nalauttaaqtausimajunik qattiraaqpammangaata <strong>aanniaviliaqpattunut</strong> qangattautiit.\",\n" +
        "                    \"Mr. Havioyak: Okay if it is under there can you give me a rough estimate as to how much you spend on <strong>medical travel</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"patient\": [\n" +
        "                [\n" +
        "                    \"<strong>aanniaviliaqsimajut</strong> angirranginnit ungasiktualuullutik aksururniqarngat.\",\n" +
        "                    \"Being a <strong>patient</strong> thousands of kilometres from home is stressful at the best of times.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"patients\": [\n" +
        "                [\n" +
        "                    \"<strong>aanniaviliaqsimajut</strong> qimattaminiit, minista tusaqtitsigunnaqtutsaujuq qatsiunirmangaata <strong>aanniaviliaqsimajut</strong> qimattaminiit qikiqtaaluup mitsaani arraagulimaami.\",\n" +
        "                    \"The <strong>patients</strong> that have been left behind, I was wondering if the minister is willing to table information on how many <strong>patients</strong> were left behind in the Baffin region for one year.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"<strong>aanniaviliaqsimajut</strong> kinguvanngittumik takujauvakpaat.\",\n" +
        "                    \"Are the <strong>patients</strong> getting seen in a timely manner.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"uqarama aanniaviliaqsimajuqutivut, tukiqattunga <strong>aanniaviliaqsimajut</strong> nunavuumit.\",\n" +
        "                    \"When I say our <strong>patients</strong>, I mean <strong>patients</strong> from Nunavut.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"immaqaa <strong>aanniaviliaqsimajut</strong> pularaqattanngiluarmata aanniasiurvinnit pigiaqaraluaqtutit.\",\n" +
        "                    \"Maybe perhaps the <strong>patients</strong> do not visit at the health centres as often as they should.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"ilangi <strong>aanniaviliaqsimajut</strong> asianut tujurmiqujausuungummata asiani.\",\n" +
        "                    \"The <strong>patients</strong> at times are told to find a place to stay or to board at a home.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"qanurli, qanuiliuriaqaqqali uvvaluunniit qanuk kamagijariaqaqpat <strong>aanniaviliaqsimajut</strong>.\",\n" +
        "                    \"So, what is the process or how do you deal with the <strong>patients</strong>?\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"uqaqtii, tukisijunga ullumi, <strong>aanniaviliaqsimajut</strong> qimatta uqattariaqanngigiangita.\",\n" +
        "                    \"Mr. Speaker, it is my understanding today, that <strong>patients</strong> should not be left behind.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"uqaqtii, piliriaqaqpalliavugut tussirautiit kiinaujaqtaarusiarnirmut aulatauniarninganut <strong>aanniaviliaqpattunut</strong> tujurmiviup amisuriakkannirutaujunnaqtut 40-kannirnik illitaarlutik <strong>aanniaviliaqpattunut</strong> tikippattunut qitirmiunit jalunaimuuqtunut.\",\n" +
        "                    \"Mr. Speaker, we’re in the process now of doing an RFP for the operation of a boarding home which would increase the bed capacity to 40 beds for the <strong>patients</strong> that are coming from the Kitikmeot Region and going to Yellowknife.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"aksururutaukkaniqtuni aanniasiuqtinut iqqanaijaqtinullu, taimaimmat ulurianarutaujunnaqpuq aanniaqtuliriniujunit <strong>aanniaviliaqpattunut</strong>.\",\n" +
        "                    \"Extra stress on nurses and staff, in turn, risk the quality of health care available to <strong>patients</strong>.\"\n" +
        "                ],\n" +
        "                [\n" +
        "                    \"kisimi, ujjiqsursutalu tamatuminga, ilanginu <strong>aanniarviliarsimajunu</strong>, pitaqarunnarsutilu timinginnut amma isumagijanginnullu ajulirutigisimajanginu imaittunu aangajaaqnartunu amma/uvvalu imialungmu pilirijaujariaqartui ikajuqviujumu sivungagut ikajunginitingni inungni taiguijjiqaanginingini qanuinirijanginu atuutijumanginartanginu.\",\n" +
        "                    \"But, we’re aware that, for some <strong>patients</strong>, there can be physical and psychological addictions to drugs and/or alcohol that must be addressed through detoxification before we can help people address the causes of their dependencies.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"patients ... billeting\": [\n" +
        "                [\n" +
        "                    \"<strong>aanniaviliaqtunik</strong> tujummivialummuuqsiqattarmijugut uvvaluunnit angirrarijaujunut.\",\n" +
        "                    \"We have to put <strong>patients</strong> in the Frobisher Inn or indeed go to <strong>billeting</strong> in private homes.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"patients ... scheduled flights\": [\n" +
        "                [\n" +
        "                    \"taimaimmat aullaujjiqattarunniiqput <strong>aanniaviliaqtunik</strong> tikiinnaqattatukkut.\",\n" +
        "                    \"So, therefore, they no longer could take <strong>patients</strong> on <strong>scheduled flights</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"piece\": [\n" +
        "                [\n" +
        "                    \"uinipaggi <strong>aanniaviliaqtunut</strong> tujurmivik, iatmantanmi <strong>aanniaviliaqtunut</strong> tujurmivik, aatavaami <strong>aanniaviliaqtunut</strong> tujurmivik, qaujijumatuinnaqtunga akiujut akittuutaummangaata amma ajjigiittiaraluarmangaata.\",\n" +
        "                    \"The Winnipeg Boarding Home, the Edmonton Boarding Home, and I think the Ottawa Boarding Home, just for my <strong>piece</strong> of mind so I know that the rates are competitive and fair.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"referral\": [\n" +
        "                [\n" +
        "                    \"<strong>aanniaviliaqtunut</strong> titiraqti\",\n" +
        "                    \"Patient <strong>Referral</strong> Clerk\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"registration\": [\n" +
        "                [\n" +
        "                    \"<strong>aanniaviliaqtunut</strong> titiraqti\",\n" +
        "                    \"<strong>Registration</strong> Clerk\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"send patients\": [\n" +
        "                [\n" +
        "                    \"<strong>aanniaviliaqtunik</strong> namutuinnaq tujurmitittiqattarumannginatta.\",\n" +
        "                    \"We don’t want to <strong>send patients</strong> to just anyone’s house.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"travel ... scheduled ... patients\": [\n" +
        "                [\n" +
        "                    \"tuavirnartut, tuavirnaqtuqaliraimmat <strong>aanniaviliaqtunik</strong>.\",\n" +
        "                    \"Scheduled <strong>travel</strong> being when <strong>patients</strong> have to <strong>travel</strong> on a <strong>scheduled</strong> flight. Medivac, when we have a medivac of <strong>patients</strong>.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"travelling away ... home ... medical treatment\": [\n" +
        "                [\n" +
        "                    \"tujurmiviqar&uni <strong>aanniaviliaqtunut</strong> atuqtaujuksanik akarrijaarutillarialuuqattaqtuq.\",\n" +
        "                    \"Having a boarding <strong>home</strong> to stay in certainly makes the experience of <strong>travelling away</strong> from <strong>home</strong> for <strong>medical treatment</strong> much more comfortable.\"\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"undergo medical travel ... medical appointments\": [\n" +
        "                [\n" +
        "                    \"taanna tammaatirvik isumaalugijaunginnaqattarngat tamakkunanga tamaunga <strong>aanniaviliaqpattunut</strong>.\",\n" +
        "                    \"The boarding home continues to be a concern to my constituents who have to <strong>undergo medical travel</strong> to Iqaluit to attend to <strong>medical appointments</strong>.\"\n" +
        "                ]\n" +
        "            ]\n" +
        "        },\n" +
        "        \"lang\": \"iu\",\n" +
        "        \"morphDecomp\": [\n" +
        "            {\n" +
        "                \"canonicalForm\": \"aanniaq\",\n" +
        "                \"grammar\": \"verb root\",\n" +
        "                \"id\": \"aanniaq/1v\",\n" +
        "                \"meaning\": \"to be in pain; to be sick; to be a patient\"\n" +
        "            },\n" +
        "            {\n" +
        "                \"canonicalForm\": \"vik\",\n" +
        "                \"grammar\": \"verb-to-noun suffix\",\n" +
        "                \"id\": \"vik/3vn\",\n" +
        "                \"meaning\": \"place where the action of the verb takes place\"\n" +
        "            },\n" +
        "            {\n" +
        "                \"canonicalForm\": \"liaq\",\n" +
        "                \"grammar\": \"noun-to-verb suffix\",\n" +
        "                \"id\": \"liaq/2nv\",\n" +
        "                \"meaning\": \"motion towards: 'to go to'; 'to go hunting' with game-animal nouns, when the hunter knows where to head\"\n" +
        "            },\n" +
        "            {\n" +
        "                \"canonicalForm\": \"jusik\",\n" +
        "                \"grammar\": \"intransitive verb ending; gerundive 2nd person dual\",\n" +
        "                \"id\": \"jusik/tv-ger-2d\",\n" +
        "                \"meaning\": \"declaration: you (two) ...\"\n" +
        "            }\n" +
        "        ],\n" +
        "        \"origWordTranslations\": [\n" +
        "            \"medical travel\",\n" +
        "            \"clients\",\n" +
        "            \"medical care\",\n" +
        "            \"medical patients\",\n" +
        "            \"regional hospital\"\n" +
        "        ],\n" +
        "        \"relatedWordTranslations\": [\n" +
        "            \"patients\",\n" +
        "            \"medical travel\",\n" +
        "            \"medical\",\n" +
        "            \"boarding\",\n" +
        "            \"piece\",\n" +
        "            \"client\",\n" +
        "            \"patient\",\n" +
        "            \"referral\",\n" +
        "            \"registration\",\n" +
        "            \"send patients\",\n" +
        "            \"medical reasons\",\n" +
        "            \"medical appointments\",\n" +
        "            \"patients ... billeting\",\n" +
        "            \"patients ... scheduled flights\",\n" +
        "            \"travel ... scheduled ... patients\",\n" +
        "            \"boarding ... clients ... medical travel\",\n" +
        "            \"travelling away ... home ... medical treatment\",\n" +
        "            \"undergo medical travel ... medical appointments\"\n" +
        "        ],\n" +
        "        \"relatedWords\": [\n" +
        "            \"aanniaviliaqsimajut\",\n" +
        "            \"aanniaviliaqpattunut\",\n" +
        "            \"aanniaviliaqtunut\",\n" +
        "            \"aanniaviliaqtunik\",\n" +
        "            \"aanniarviliarsimajunu\"\n" +
        "        ],\n" +
        "        \"word\": \"aanniaviliaqtutik\",\n" +
        "        \"wordInOtherScript\": \"nevermindThisCannotWriteUTF8charsInJSPFile\",\n" +
        "        \"wordRoman\": \"aanniaviliaqtutik\",\n" +
        "        \"wordSyllabic\": \"nevermindThisCannotWriteUTF8charsInJSPFile\"\n" +
        "    },\n" +
        "    \"stackTrace\": null,\n" +
        "    \"status\": null,\n" +
        "    \"taskElapsedMsecs\": 578,\n" +
        "    \"taskID\": \"2021-11-12T10:31:06.957Z\",\n" +
        "    \"taskStartTime\": 1636713066957,\n" +
        "    \"totalWords\": 2\n" +
        "}"
    );
</script>
