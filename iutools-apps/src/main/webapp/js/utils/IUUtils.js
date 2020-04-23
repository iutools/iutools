/**
 * Utilities for processing Iunktut text
 */


class IUUtils {
		
	constructor() {
//		this.syllabicChars = [
//            '\u1449', '\u1466', '\u1483', '\u14A1', '\u14BB', '\u14D0', 
//            '\u1505', '\u14EA', '\u153E', '\u155D', '\u1550', '\u1585', 
//            '\u1595', '\u1596', '\u15A6', '\u157C', '\u15AF'
//		];
//		this.romanChars = [
//			"a","i","u","g","j","k","l","m","n","p","q","r","s","t","v","&"
//		];
	}
	
	static isInuktut(text) {
		return this.isSyllabic(text)|| this.isRoman(text);
	}
	
	static isRoman(text) {
		var res = true;
		for (var ich=0; ich<text.length; ich++)
			if ( !IUUtils.romanChars.includes(text.charAt(ich)) ) {
				res = false;
				break;
			}
		return res;
	}
	
	static isSyllabic(text) {
		var res = true;
		for (var ich=0; ich<text.length; ich++)
			if ( !IUUtils.syllabicChars.includes(text.charAt(ich)) ) {
				res = false;
				break;
			}
		return res;
	}
} 

IUUtils.syllabicChars = [
	'\u1403', '\u1431', '\u144E', '\u146D', '\u148B', '\u14A5', '\u14C2', 
	'\u14EF', '\u14D5', '\u1528', '\u1555', '\u1546', '\u157F', '\u158F', 
	'\u1671', '\u15A0', '\u1405', '\u1433', '\u1450', '\u146F', '\u148D', 
	'\u14A7', '\u14C4', '\u14F1', '\u14D7', '\u152A', '\u1557', '\u1548', 
	'\u1581', '\u1591', '\u1673', '\u15A2', '\u140A', '\u1438', '\u1455', 
	'\u1472', '\u1490', '\u14AA', '\u14C7', '\u14F4', '\u14DA', '\u152D', 
	'\u1559', '\u154B', '\u1583', '\u1593', '\u1675', '\u15A4', '\u1404', 
	'\u1432', '\u144F', '\u146E', '\u148C', '\u14A6', '\u14C3', '\u14F0', 
	'\u14D6', '\u1529', '\u1556', '\u1547', '\u1580', '\u1590', '\u1672', 
	'\u15A1', '\u1406', '\u1434', '\u1451', '\u1470', '\u148E', '\u14A8', 
	'\u14C5', '\u14F2', '\u14D8', '\u152B', '\u1558', '\u1549', '\u1582', 
	'\u1592', '\u1674', '\u15A3', '\u140B', '\u1439', '\u1456', '\u1473', 
	'\u1491', '\u14AB', '\u14C8', '\u14F5', '\u14DB', '\u152E', '\u155A', 
	'\u154C', '\u1584', '\u1594', '\u1676', '\u15A5', '\u1449', '\u1466', 
	'\u1483', '\u14A1', '\u14BB', '\u14D0', '\u1505', '\u14EA', '\u153E', 
	'\u155D', '\u1550', '\u1585', '\u1595', '\u1596', '\u15A6', '\u157C', 
	'\u15AF', 
];

IUUtils.romanChars = [
	"a","i","u","g","j","k","l","m","n","p","q","r","s","t","v","&"
];