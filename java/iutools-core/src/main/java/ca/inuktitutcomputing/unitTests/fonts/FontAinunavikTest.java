/*
 * Conseil national de recherche Canada 2006/
 * National Research Council Canada 2006
 * 
 * Cr�� le / Created on Oct 24, 2006
 * par / by Benoit Farley
 * 
 */
package ca.inuktitutcomputing.unitTests.fonts;

import ca.inuktitutcomputing.fonts.FontAinunavik;
import junit.framework.TestCase;

public class FontAinunavikTest extends TestCase {

    /*
     * Test method for 'fonts.PoliceAinunavik.transcodeToUnicode(String, String)'
     */
    public void testTranscodeToUnicodeStringString() {
        String res = null;
        /*
         * pinasualaursimanngituq, atanngiijjutauniaqpuq, nunannguatigut, 
         * kinnguumajanginnik, tunngasugitsi, uqautivinngaa, kangiq&iniup,
         * ajjingiinngittuniinniaqtutik, angutiillu, arraanguulaaqtumi, 
         * gavamangannik, maligaliurvingmiingaarnikuuvugut, utarqiniq, 
         * utarqiinnaqtuq, qimirqujiujut, ilitarirquupuq, iniurqavik, itirqaaqpunga, 
         * ippigitsiaruk, taku&ugit, ikak&ak,
         * aippiqtaulluni, paippaamullu, taikkua, kaivallalluaqsunga, turaagait, 
         * tamaita, nalunairlugu, tungaliritsainartilugit, allait, nunalituqatsajait, 
         * sanirvaisimavugut, nutarait, qaisimalaurtutuit, tamakkuningaiglunit,
         * saanngainnaraktigu, tuqsunuurqaivuq
         * 
         * uvvaluunniit iilaak
         * Haamlaujunit angillivaalliarjussimajuumuppuq taakkuangu&&aqpuq
         * uumajur&aasuut i&uuraarjungnik sivu&iinnaq marruuliqtuunni iluunnaaguut
         * nunavuulimaaq sapuulutait tamatumuuna aaggaa aajiiqatigiinniq
         * siarrijaarviit unikkaaliulauqpiit iliilauqpisii kiinaujallariit
         * 
         * maqruuk
         */
        String [][] words = {
                // 0. n+ngi: pinasualaursimanngituq
                {"WNhxMs3ym1qg6","\u1431\u14c7\u14f1\u140a\u14da\u1405\u1550\u14ef\u14aa\u1671\u1450\u1585"},
                // 1. n+ngii: atanngiijjutauniaqpuq
                {"xb8\u01530Jbsix6S6","\u140a\u1455\u1672\u153e\u152a\u1455\u1405\u14c2\u140a\u1585\u1433\u1585"},
                // 2. n+ngu: nunannguatigut
                {"kN8axtA5","\u14c4\u14c7\u1673\u140a\u144e\u148d\u1466"},
                // 3. n+nguu: kinnguumajanginnik
                {"r8\u00e5m/q8i4","\u146d\u1674\u14aa\u152d\u158f\u14d0\u14c2\u1483"},
                // 4. n+nga: tunngasugitsi
                {"g8zhQ5y","\u1450\u1675\u14f1\u148b\u1466\u14ef"},
                // 5. n+ngaa: uqautivinngaa
                {"scst[8\u00af","\u1405\u1583\u1405\u144e\u1555\u1676"},
                // 6. ngi: kangiq&iniup
                {"vq6\u00C3is2","\u1472\u158f\u1585\u15a0\u14c2\u1405\u1449"},
                // 7. ngii: ajjingiinngittuniinniaqtutik
                {"x0p\u01538q5g\u00EE8ix6gt4","\u140a\u153e\u1528\u1590\u1671\u1466\u1450\u14c3\u14d0\u14c2\u140a\u1585\u1450\u144e\u1483"},
                // 8. ngu: angutiillu
                {"xa\u20209l","\u140a\u1591\u144f\u14ea\u14d7"},
                // 9. nguu: arraanguulaaqtumi
                {"x3\u00C7\u00E5\u00986gu","\u140a\u1550\u154c\u1592\u14db\u1585\u1450\u14a5"},
                // 10. nga: gavamangannik
                {"Z?mz8i4","\u1490\u1559\u14aa\u1593\u14d0\u14c2\u1483"},
                // 11. ngaa: maligaliurvingmiingaarnikuuvugut
                {"moZos3[1\u00FC\u00AF3i\u0192KA5","\u14aa\u14d5\u1490\u14d5\u1405\u1550\u1555\u1595\u14a6\u1594\u1550\u14c2\u1470\u1557\u148d\u1466"},
                
                // 12. rqi: utarqiniq
                {"sb3ei6","\u1405\u1455\u1585\u146d\u14c2\u1585"},
                // 13. rqii: utarqiinnaqtuq
                {"sb3\u00E98N6g6","\u1405\u1455\u1585\u146e\u14d0\u14c7\u1585\u1450\u1585"},
                // 14. qri: niqriku
                {"i6Ef","\u14c2\u1550\u1546\u146f"},
                // 15. qrii: taqriijaamit
                {"b6\u0089\u00F7u5","\u1455\u1550\u1547\u152e\u14a5\u1466"},
                // 16. qqi: utaqqiniq
                {"sb6ei6","\u1405\u1455\u1585\u146d\u14c2\u1585"},
                // 17. qqii: utaqqiinnaqtuq
                {"sb6\u00E98N6g6","\u1405\u1455\u1585\u146e\u14d0\u14c7\u1585\u1450\u1585"},
                // 18. rqu: qimirqujiujut
                {"eu3dpsJ5","\u157f\u14a5\u1585\u146f\u1528\u1405\u152a\u1466"},
                // 19. rquu: ilitarirquupuq
                {"wobE3\u00DAS6","\u1403\u14d5\u1455\u1546\u1585\u1470\u1433\u1585"},
                // 20. qru: qimiqruagait
                {"eu6DxZw5","\u157f\u14a5\u1550\u1548\u140a\u1490\u1403\u1466"},
                // 21. qruu: maqruuk
                {"m6\u00CE4","\u14aa\u1550\u1549\u1483"},
                // 22. qqu: qimiqqujiujut
                {"eu6dpsJ5","\u157f\u14a5\u1585\u146f\u1528\u1405\u152a\u1466"},
                // 23. qquu: ilitariqquupuq
                {"wobE6\u00DAS6","\u1403\u14d5\u1455\u1546\u1585\u1470\u1433\u1585"},
                // 24. rqa: iniurqavik
                {"wis3c[4","\u1403\u14c2\u1405\u1585\u1472\u1555\u1483"},
                // 25. rqaa: itirqaaqpunga
                {"wt3\u00E76Sz","\u1403\u144e\u1585\u1473\u1585\u1433\u1593"},
                // 26. qra: angiqrapaa
                {"xq6C\u00D9","\u140a\u158f\u1550\u154b\u1439"},
                // 27. qraa: aqraagu
                {"x6\u00C7A","\u140a\u1550\u154c\u148d"},
                // 28. qqa: iniuqqavik
                {"wis6c[4","\u1403\u14c2\u1405\u1585\u1472\u1555\u1483"},
                // 29. qqaa: itiqqaaqpunga
                {"wt6\u00E76Sz","\u1403\u144e\u1585\u1473\u1585\u1433\u1593"},

                // 30. ru: ippigitsiaruk
                {"w2WQ5yxD4","\u1403\u1449\u1431\u148b\u1466\u14ef\u140a\u1548\u1483"},
                // 31. &u: taku&ugit
                {"bf\u00C0Q5","\u1455\u146f\u15a2\u148b\u1466"},
                // 32. &a: ikak&ak
                {"wv4\u009F4","\u1403\u1472\u1483\u15a4\u1483"},
                // 33. luu, nii, v: uvvaluunniit
                {"s{?\u00AC8\u00EE5","\u1405\u155d\u1559\u14d8\u14d0\u14c3\u1466"},
                // 34.35. ii, laa: iilaak
                {"\u2122\u00984","\u1404\u14db\u1483"},
                {"\u0099\u00984","\u1404\u14db\u1483"},
                // 36. H, aa, m: Haamlaujunit
                {"\u00FF\u00807MsJi5","\u157c\u140b\u14bb\u14da\u1405\u152a\u14c2\u1466"},
                // 37. vaa, s, juu: angillivaalliarjussimajuumuppuq
                {"xq9o\u00BF9ox3J+ym\u00D4j2S6",
                    "\u140a\u158f\u14ea\u14d5\u155a\u14ea\u14d5\u140a\u1550\u152a\u1505\u14ef\u14aa\u152b\u14a7\u1449\u1433\u1585"},
                // 38. taa, &: taakkuangu&&aqpuq
                {"\u00CC4fxa\u00D5\u009F6S6","\u1456\u1483\u146f\u140a\u1591\u15a6\u15a4\u1585\u1433\u1585"},
                // 39. uu, &aa, suu: uumajur&aasuut
                {"\u00DFmJ3\u00EF\u00A75","\u1406\u14aa\u152a\u1550\u15a5\u14f2\u1466"},
                // 40. &uu, raa, ng: i&uuraarjungnik
                {"w\u00EC\u00C73J1i4","\u1403\u15a3\u154c\u1550\u152a\u1595\u14c2\u1483"},
                // 41. &ii: sivu&iinnaq
                {"yK\u00ED8N6","\u14ef\u1557\u15a1\u14d0\u14c7\u1585"},
                // 42. ruu, tuu: marruuliqtuunni
                {"m3\u00CEo6\u00A98i","\u14aa\u1550\u1549\u14d5\u1585\u1451\u14d0\u14c2"},
                // 43. luu, naa, guu: iluunnaaguut
                {"w\u00AC8\u0088\u00C55","\u1403\u14d8\u14d0\u14c8\u148e\u1466"},
                // 44. vuu, maa: nunavuulimaaq
                {"kN\u00D3o\u00B56","\u14c4\u14c7\u1558\u14d5\u14ab\u1585"},
                // 45. puu: sapuulutait
                {"n\u00CDlbw5","\u14f4\u1434\u14d7\u1455\u1403\u1466"},
                // 46. muu: tamatumuuna
                {"bmg\u00CBN","\u1455\u14aa\u1450\u14a8\u14c7"},
                // 47. aa, gaa: aaggaa
                {"\u0080=\u00DB","\u140b\u14a1\u1491"},
                // 48. aa, jii, gii: aajiiqatigiinniq
                {"\u20AC\u00BAct\u01528i6","\u140b\u1529\u1583\u144e\u148c\u14d0\u14c2\u1585"},
                // 49. jaa, vii: siarrijaarviit
                {"yx3E\u00F73\u00935","\u14ef\u140a\u1550\u1546\u152e\u1550\u1556\u1466"},
                // 50. kaa, pii: unikkaaliulauqpiit
                {"si4\u00CFosMs6\u00845","\u1405\u14c2\u1483\u1473\u14d5\u1405\u14da\u1405\u1585\u1432\u1466"},
                // 51. lii, sii: iliilauqpisii
                {"w\u00F8Ms6W\u00A5","\u1403\u14d6\u14da\u1405\u1585\u1431\u14f0"},
                // 52. kii, rii: kiinaujallariit
                {"\u00AENsI9M\u00895","\u146e\u14c7\u1405\u152d\u14ea\u14da\u1547\u1466"},
        };

        String wordsAI[][] = {
                // 0. ai: aippiqtaulluni
                {"xw2W6bs9li","\u00C92W6bs9li",
                    "\u140a\u1403\u1449\u1431\u1585\u1455\u1405\u14ea\u14d7\u14c2",
                    "\u1401\u1449\u1431\u1585\u1455\u1405\u14ea\u14d7\u14c2"
                },
                // 1. pai: paippaamullu
                {"Xw2\u00D9j9l","\u00D12\u00D9j9l",
                    "\u1438\u1403\u1449\u1439\u14a7\u14ea\u14d7",
                    "\u142f\u1449\u1439\u14a7\u14ea\u14d7"
                },
                // 2. tai: taikkua
                {"bw4fx","\u00D64fx",
                    "\u1455\u1403\u1483\u146f\u140a",
                    "\u144c\u1483\u146f\u140a"
                },
                // 3. kai: kaivallalluaqsunga
                {"vw?9M9lx6hz","\u00DC?9M9lx6hz",
                    "\u1472\u1403\u1559\u14ea\u14da\u14ea\u14d7\u140a\u1585\u14f1\u1593",
                    "\u146b\u1559\u14ea\u14da\u14ea\u14d7\u140a\u1585\u14f1\u1593"
                 },
                // 4. gai: turaagait
                {"g\u00C7Zw5","g\u00C7\u00E15",
                     "\u1450\u154c\u1490\u1403\u1466",
                     "\u1450\u154c\u1489\u1466"
                },
                // 5. mai: tamaita
                {"bmwb","b\u00E0b",
                    "\u1455\u14aa\u1403\u1455",
                    "\u1455\u14a3\u1455"
                },
                // 6. nai: nalunairlugu
                {"NlNw3lA","Nl\u00E23lA",
                    "\u14c7\u14d7\u14c7\u1403\u1550\u14d7\u148d",
                    "\u14c7\u14d7\u14c0\u1550\u14d7\u148d"
                },
                // 7. sai: tungaliritsainartilugit
                {"gzoE5nwN3tlQ5","gzoE5\u00E3N3tlQ5",
                    "\u1450\u1593\u14d5\u1546\u1466\u14f4\u1403\u14c7\u1550\u144e\u14d7\u148b\u1466",
                    "\u1450\u1593\u14d5\u1546\u1466\u14ed\u14c7\u1550\u144e\u14d7\u148b\u1466"
                 },
                // 8. lai: allait
                {"x9Mw5","x9\u00E45",
                    "\u140a\u14ea\u14da\u1403\u1466",
                    "\u140a\u14ea\u14d3\u1466",
                 },
                // 9. jai: nunalituqatsajait
                {"kNogc5n/w5","kNogc5n\u00E85",
                     "\u14c4\u14c7\u14d5\u1450\u1583\u1466\u14f4\u152d\u1403\u1466",
                     "\u14c4\u14c7\u14d5\u1450\u1583\u1466\u14f4\u1526\u1466"
                },
                // 10. vai: sanirvaisimavugut
                {"ni3?wymKA5","ni3\u00EBymKA5",
                    "\u14f4\u14c2\u1550\u1559\u1403\u14ef\u14aa\u1557\u148d\u1466",
                    "\u14f4\u14c2\u1550\u1553\u14ef\u14aa\u1557\u148d\u1466"
                },
                // 11. rai: nutarait
                {"kbCw5","kb\u00EA5",
                    "\u14c4\u1455\u154b\u1403\u1466",
                    "\u14c4\u1455\u1542\u1466"
                 },
                // 12. qai: qaisimalaurtutuit
                {"cwymMs3ggw5","\u00F2ymMs3ggw5",
                     "\u1583\u1403\u14ef\u14aa\u14da\u1405\u1550\u1450\u1450\u1403\u1466",
                     "\u166f\u14ef\u14aa\u14da\u1405\u1550\u1450\u1450\u1403\u1466"
                },
                // 13. ngai: tamakkuningaiglunit
                {"bm4fizw=li5","bm4fi\u00F4=li5",
                    "\u1455\u14aa\u1483\u146f\u14c2\u1593\u1403\u14a1\u14d7\u14c2\u1466",
                    "\u1455\u14aa\u1483\u146f\u14c2\u1670\u14a1\u14d7\u14c2\u1466"
                },
                // 14. n+ngai: saanngainnaraktigu
                {"\u00F18zw8NC4tA","\u00F18\u00F48NC4tA",
                    "\u14f5\u1675\u1403\u14d0\u14c7\u154b\u1483\u144e\u148d",
                    "\u14f5\u1596\u1489\u14d0\u14c7\u154b\u1483\u144e\u148d"
                },
                // 15. rqai: tuqsunuurqaivuq
                {"g6h\u00AA3cwK6","g6h\u00AA3\u00F2K6",
                    "\u1450\u1585\u14f1\u14c5\u1585\u1472\u1403\u1557\u1585",
                    "\u1450\u1585\u14f1\u14c5\u1585\u146b\u1557\u1585"
                },
                // 16. qrai: ikaqrait
                {"wv6Cw5","wv6\u00EA5",
                    "\u1403\u1472\u1550\u154b\u1403\u1466",
                    "\u1403\u1472\u1550\u1542\u1466"
                },
                // 17. qqai: tuqsunuuqqaivuq
                {"g6h\u00AA6cwK6","g6h\u00AA6\u00F2K6",
                    "\u1450\u1585\u14f1\u14c5\u1585\u1472\u1403\u1557\u1585",
                    "\u1450\u1585\u14f1\u14c5\u1585\u146b\u1557\u1585"
                },
    };
        for (int i=0; i<words.length; i++) {
            String str = words[i][0];
            String targ = words[i][1];
            res = FontAinunavik.transcodeToUnicode(str,null);
            assertEquals("1."+i+". "+res+" au lieu de "+targ,targ,res);
        }
        for (int i=0; i<wordsAI.length; i++) {
            String str = wordsAI[i][0];
            String targ = wordsAI[i][2];
            res = FontAinunavik.transcodeToUnicode(str,null);
            assertEquals("2a."+i+". "+res+" au lieu de "+targ,targ,res);
            str = wordsAI[i][1];
            res = FontAinunavik.transcodeToUnicode(str,null);
            assertEquals("2b."+i+". "+res+" au lieu de "+targ,targ,res);
        }
        for (int i=0; i<wordsAI.length; i++) {
            String str = wordsAI[i][0];
            String targ = wordsAI[i][3];
            res = FontAinunavik.transcodeToUnicode(str,"aipaitai");
            assertEquals("3a."+i+". "+res+" au lieu de "+targ,targ,res);
            str = wordsAI[i][1];
            res = FontAinunavik.transcodeToUnicode(str,"aipaitai");
            assertEquals("3b."+i+". "+res+" au lieu de "+targ,targ,res);
        }

    }

    /*
     * Test method for 'fonts.PoliceAinunavik.transcodeFromUnicode(String)'
     */
    public void testTranscodeFromUnicode() {

    }

}
