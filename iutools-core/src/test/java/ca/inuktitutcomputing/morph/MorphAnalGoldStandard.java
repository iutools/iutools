package ca.inuktitutcomputing.morph;

import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides gold standard answers that the morphological 
 * analyzer should return for a series of words.
 * 
 * @author desilets
 *
 */
public class MorphAnalGoldStandard {
	
	Map<String,AnalyzerCase> case4word = 
			new HashMap<String,AnalyzerCase>();
	
	public MorphAnalGoldStandard() throws Exception {
		initCases();
	}
	
	public void addCase(AnalyzerCase caseData) {
		case4word.put(caseData.word, caseData);
	}
	
	public Set<String> allWords() {
		return case4word.keySet();
	}

	public String correctDecomp(String word) {
		AnalyzerCase anlCase = case4word.get(word);
		return anlCase.correctDecomp;
	}
		
	public AnalyzerCase caseData(String word) {
		return case4word.get(word);
	}
	
	private void initCases() throws Exception {
		addCase(new AnalyzerCase("Haammalakkut", "{Haammala:Haammalat/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("Hamlakkut", "{Hamla:Hamlat/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("Hanta", "[decomposition:/Hanta(Hanta)/]")
			.isProperName());
		addCase(new AnalyzerCase("Haviujaq", "[decomposition:/Haviujaq(Haviujaq)/]")
			.isProperName());
		addCase(new AnalyzerCase("aagga", "{aagga:aakka/1a}"));
		addCase(new AnalyzerCase("aanniaqtulirijikkunnut", "{aanniaq:aanniaq/1v}{tu:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("aanniaqtulirijikkut", "{aanniaq:aanniaq/1v}{tu:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("aanniaqtulirinirmut", "{aanniaq:aanniaq/1v}{tu:juq/1vn}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("aanniaviliaqtunut", "{aannia:aanniaq/1v}{vi:vik/3vn}{liaq:liaq/2nv}{tu:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("aaqqiumainnarutinut", "{aaqqi:aaqqik/1v}{uma:ma/1vv}{inna:innaq/2vv}{ruti:ut/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("aglattiu", "{aglat:allak/1v}{ti:ji/1vn}{u:up/tn-gen-s}"));
		addCase(new AnalyzerCase("agluukkaq", null)
			.isProperName()
			.comment("Family name"));
		addCase(new AnalyzerCase("aippaanik", "{aippa:aippaq/1n}{anik:nganik/tn-acc-s-4s}"));
		addCase(new AnalyzerCase("aippaanit", "{aippa:aippaq/1n}{anit:nganit/tn-abl-s-4s}"));
		addCase(new AnalyzerCase("aippanga", "{aippa:aippaq/1n}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("aipuru", "{aipuru:iipuru/1n}"));
		addCase(new AnalyzerCase("ajauqtiit", "{aja:ajak/1v}{uq:uq/3vv}{ti:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ajjaqsijii", "{ajjaq:arjaq/1v}{si:si/1vv}{ji:ji/1vn}{i:k/tn-nom-d}"));
		addCase(new AnalyzerCase("ajjaqsiji", "{ajjaq:arjaq/1v}{si:si/1vv}{ji:ji/1vn}"));
		addCase(new AnalyzerCase("ajjigiinngittunik", "{ajji:ajji/1n}{gii:giik/2nv}{nngit:nngit/1vv}{tu:juq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("ajjigiinngittunit", "{ajji:ajji/1n}{gii:giik/2nv}{nngit:nngit/1vv}{tu:juq/1vn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("ajjigiinngittunut", "{ajji:ajji/1n}{gii:giik/2nv}{nngit:nngit/1vv}{tu:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ajjigiinngittut", "{ajji:ajji/1n}{gii:giik/2nv}{nngit:nngit/1vv}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("akinga", "{aki:aki/1n}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("akinginnut", "{aki:aki/1n}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("akingit", "{aki:aki/1n}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("akiqangittuq", "{aki:aki/1n}{qa:qaq/1nv}{ngit:nngit/1vv}{tuq:juq/1vn}")
			.isMisspelled());
		addCase(new AnalyzerCase("akiraqtuqtut", "{akiraq:akiraq/1v}{tuq:tuq/1vv}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("akisuk", null)
			.isProperName()
			.comment("Family name"));
		addCase(new AnalyzerCase("akitujunut", "{aki:aki/1n}{tu:tu/1nv}{ju:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("akitujutinut", "{aki:aki/1n}{tu:tu/1nv}{ju:juq/1vn}{ti:ut/2nn}{nut:nut/tn-dat-p}")
			.isMisspelled());
		addCase(new AnalyzerCase("akitujuutinut", "{aki:aki/1n}{tu:tu/1nv}{ju:juq/1vn}{uti:ut/2nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("aksualuk", "{aksualuk:aksualuk/1a}"));
		addCase(new AnalyzerCase("akulliq", "{aku:aku/1n}{lliq:&iq/1nn}"));
		addCase(new AnalyzerCase("akuni", "{akuni:akuni/1n}"));
		addCase(new AnalyzerCase("akunialuk", "{akuni:akuni/1n}{aluk:aluk/1nn}"));
		addCase(new AnalyzerCase("alakannuaq", null)
			.isProperName()
			.comment("Family name"));
		addCase(new AnalyzerCase("alakkannuaq", null)
			.isProperName()
			.comment("Family name"));
		addCase(new AnalyzerCase("alianaigusukpunga", "{alianai:alianait/1v}{gusuk:gusuk/1vv}{punga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("allaat", "{allaat:allaat/1a}"));
		addCase(new AnalyzerCase("allatti", "{allat:allak/1v}{ti:ji/1vn}"));
		addCase(new AnalyzerCase("amisuni", "{amisu:amisu/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("amisunik", "{amisu:amisu/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("amisunit", "{amisu:amisu/1n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("amisunut", "{amisu:amisu/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("amisut", "{amisut:amisut/1n}"));
		addCase(new AnalyzerCase("amisuummata", "{amisu:amisu/1n}{u:u/1nv}{mmata:mata/tv-caus-4p}"));
		addCase(new AnalyzerCase("amisuuningit", "{amisu:amisu/1n}{u:u/1nv}{ni:niq/2vn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("amittuq", "{amit:amit/1v}{tuq:juq/1vn}"));
		addCase(new AnalyzerCase("amittuq", null)
			.isProperName()
			.comment("community name"));
		addCase(new AnalyzerCase("amma", "{amma:amma/1c}"));
		addCase(new AnalyzerCase("ammalu", "{amma:amma/1c}{lu:lu/1q}"));
		addCase(new AnalyzerCase("ammaluttauq", "{amma:amma/1c}{lu:lu/1q}{ttauq:ttauq/1q}"));
		addCase(new AnalyzerCase("ammattauq", "{amma:amma/1c}{ttauq:ttauq/1q}"));
		addCase(new AnalyzerCase("anaruaq", null)
			.isProperName()
			.comment("family name"));
		addCase(new AnalyzerCase("angijumik", "{angi:angi/1v}{ju:juq/1vn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("anginngittut", "{angi:angi/1v}{nngit:nngit/1vv}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("anginngittut", "{angi:angiq/1v}{nngit:nngit/1vv}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("angiqpisi", "{angiq:angiq/1v}{pisi:visi/tv-int-2p}"));
		addCase(new AnalyzerCase("angiqpisii", "{angiq:angiq/1v}{pisii:visii/tv-int-2p}"));
		addCase(new AnalyzerCase("angiqpugut", "{angiq:angiq/1v}{pugut:vugut/tv-dec-1p}"));
		addCase(new AnalyzerCase("angiqtugut", "{angiq:angiq/1v}{tugut:jugut/tv-ger-1p}"));
		addCase(new AnalyzerCase("angiqtut", "{angiq:angiq/1v}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("anniaqarnangittulirinirmi", "{annia:aanniaq/1n}{qar:qaq/1nv}{nangit:nanngit/1vv}{tu:juq/1vn}{liri:liri/1nv}{nir:niq/2vn}{mi:mi/tn-loc-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("apiqqusiit", "{apiq:apiq/1v}{qusi:usiq/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("apiqqusiksait", "{apiq:apiq/1v}{qusi:usiq/1vn}{ksa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("apiqqusira", "{apiq:apiq/1v}{qusi:usiq/1vn}{ra:ga/tn-nom-s-1s}"));
		addCase(new AnalyzerCase("apiqqusirijara", "{apiq:apiq/1v}{qusi:usiq/1vn}{ri:gi/1nv}{jara:jara/tv-ger-1s-3s}"));
		addCase(new AnalyzerCase("apiqqusirnut", "{apiq:apiq/1v}{qusir:usiq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("apiqqut", "{apiq:apiq/1v}{qut:ut/1vn}"));
		addCase(new AnalyzerCase("apiqquti", "{apiq:apiq/1v}{quti:ut/1vn}"));
		addCase(new AnalyzerCase("apiqqutiga", "{apiq:apiq/1v}{quti:ut/1vn}{ga:ga/tn-nom-s-1s}"));
		addCase(new AnalyzerCase("apiqqutigali", "{apiq:apiq/1v}{quti:ut/1vn}{ga:ga/tn-nom-s-1s}{li:li/1q}"));
		addCase(new AnalyzerCase("apiqqutigijara", "{apiq:apiq/1v}{quti:ut/1vn}{gi:gi/1nv}{jara:jara/tv-ger-1s-3s}"));
		addCase(new AnalyzerCase("apiqqutiit", "{apiq:apiq/1v}{quti:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("apiqqutik", "{apiq:apiq/1v}{qutik:ut/1vn}"));
		addCase(new AnalyzerCase("apiqqutiksait", "{apiq:apiq/1v}{quti:ut/1vn}{ksa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("apiqqutinga", "{apiq:apiq/1v}{quti:ut/1vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("apiqqutinganut", "{apiq:apiq/1v}{quti:ut/1vn}{nganut:nganut/tn-dat-s-4s}"));
		addCase(new AnalyzerCase("apiqqutinut", "{apiq:apiq/1v}{quti:ut/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("apiqqutissait", "{apiq:apiq/1v}{quti:ut/1vn}{ssa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("apiqqutit", "{apiq:apiq/1v}{qut:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("apiqqutitsait", "{apiq:apiq/1v}{quti:ut/1vn}{tsa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("apirijumajara", "{apiri:apiri/1v}{juma:juma/1vv}{jara:jara/tv-ger-1s-3s}"));
		addCase(new AnalyzerCase("apirijumajunga", "{apiri:apiri/1v}{juma:juma/1vv}{junga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("apirijumavara", "{apiri:apiri/1v}{juma:juma/1vv}{vara:vara/tv-dec-1s-3s}"));
		addCase(new AnalyzerCase("arnait", "{arna:arnaq/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("arraagu", "{arraagu:arraagu/1n}"));
		addCase(new AnalyzerCase("arraaguit", "{arraagu:arraagu/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("arraagumi", "{arraagu:arraagu/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("arraagumut", "{arraagu:arraagu/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("arraagunik", "{arraagu:arraagu/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("arraagunit", "{arraagu:arraagu/1n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("arraagunut", "{arraagu:arraagu/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("arraagutamaat", "{arraagu:arraagu/1n}{tamaa:tamaaq/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("arraaguttinni", "{arraagu:arraagu/1n}{ttinni:ttinni/tn-loc-s-1d}"));
		addCase(new AnalyzerCase("arraaguup", "{arraagu:arraagu/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("arraani", "{arraani:arraani/1n}"));
		addCase(new AnalyzerCase("arragumi", "{arragu:arraagu/1n}{mi:mi/tn-loc-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("arvaalluk", null)
			.isProperName()
			.comment("family name"));
		addCase(new AnalyzerCase("arvaaluk", null)
			.isProperName()
			.comment("family name"));
		addCase(new AnalyzerCase("arvaarluk", null)
			.isProperName()
			.comment("family name"));
		addCase(new AnalyzerCase("arviani", "{arvia:arviat/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("arviat", "{arviat:arviat/1n}"));
		addCase(new AnalyzerCase("asiagut", "{asi:asi/1n}{agut:ngagut/tn-via-s-4s}"));
		addCase(new AnalyzerCase("asiani", "{asi:asi/1n}{ani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("asianik", "{asi:asi/1n}{anik:nganik/tn-acc-s-4s}"));
		addCase(new AnalyzerCase("asianut", "{asi:asi/1n}{anut:nganut/tn-dat-s-4s}"));
		addCase(new AnalyzerCase("asingi", "{asi:asi/1n}{ngi:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("asingillu", "{asi:asi/1n}{ngil:ngit/tn-nom-p-4s}{lu:lu/1q}"));
		addCase(new AnalyzerCase("asinginni", "{asi:asi/1n}{nginni:nginni/tn-loc-p-4s}"));
		addCase(new AnalyzerCase("asinginnik", "{asi:asi/1n}{nginnik:nginnik/tn-acc-p-4s}"));
		addCase(new AnalyzerCase("asinginnillu", "{asi:asi/1n}{nginnil:nginnit/tn-abl-p-4s}{lu:lu/1q}"));
		addCase(new AnalyzerCase("asinginnit", "{asi:asi/1n}{nginnit:nginnit/tn-abl-p-4s}"));
		addCase(new AnalyzerCase("asinginnullu", "{asi:asi/1n}{nginnul:nginnut/tn-dat-p-4s}{lu:lu/1q}"));
		addCase(new AnalyzerCase("asingit", "{asi:asi/1n}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("asuillaak", "{asuillaak:asuilaak/1a}")
			.isMisspelled());
		addCase(new AnalyzerCase("ataani", "{ata:ata/1n}{ani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("atausiq", "{atausiq:atausiq/1n}"));
		addCase(new AnalyzerCase("atausirmi", "{atausir:atausiq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("atausirmik", "{atausir:atausiq/1n}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("atausirmut", "{atausir:atausiq/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("atii", "{atii:atii/1e}"));
		addCase(new AnalyzerCase("atiliurutausimajut", "{ati:atiq/1n}{liu:liuq/1nv}{ruta:ut/1vn}{u:u/1nv}{sima:sima/1vv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("atinga", "{ati:atiq/1n}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("atingit", "{ati:atiq/1n}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("atirnik", "{atir:atiq/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("atsualuk", "{atsualuk:aksualuk/1a}"));
		addCase(new AnalyzerCase("atuagait", "{atu:atuq/1v}{a:a/1vv}{ga:gaq/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("atuagaq", "{atu:atuq/1v}{a:a/1vv}{gaq:gaq/1vn}"));
		addCase(new AnalyzerCase("atuagarmik", "{atu:atuq/1v}{a:a/1vv}{gar:gaq/1vn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("atuagarnik", "{atu:atuq/1v}{a:a/1vv}{gar:gaq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("atuliqujaujuq", "{atu:atuq/1v}{li:li/2vv}{qu:qu/2vv}{ja:jaq/1vn}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("atuni", "{atuni:atunit/1n}"));
		addCase(new AnalyzerCase("atunit", "{atunit:atunit/1n}"));
		addCase(new AnalyzerCase("atuqtuksait", "{atuq:atuq/1v}{tuksa:juksaq/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("atuqtuksanut", "{atuq:atuq/1v}{tuksa:juksaq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("aujakkut", "{auja:aujaq/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("aulajjutinut", "{aula:aula/1v}{jjuti:ut/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("aulaninginnut", "{aula:aula/1v}{ni:niq/2vn}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("aulanirmut", "{aula:aula/1v}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("aulattijiit", "{aulat:aulat/1v}{ti:si/1vv}{ji:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("aulattinirmut", "{aulat:aulat/1v}{ti:si/1vv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("aviktursimajuni", "{avik:avik/1v}{tur:tuq/1vv}{sima:sima/1vv}{ju:juq/1vn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("avittuqsimajuni", "{avit:avik/1v}{tuq:tuq/1vv}{sima:sima/1vv}{ju:juq/1vn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("avittuqsimajunut", "{avit:avik/1v}{tuq:tuq/1vv}{sima:sima/1vv}{ju:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("avittuqsimajut", "{avit:avik/1v}{tuq:tuq/1vv}{sima:sima/1vv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("avittuqsimajutigut", "{avit:avik/1v}{tuq:tuq/1vv}{sima:sima/1vv}{ju:juq/1vn}{tigut:tigut/tn-via-p}"));
		addCase(new AnalyzerCase("gaasalii", "{gaasalii:gaasalii/1n}"));
		addCase(new AnalyzerCase("gavama", "{gavama:gavama/1n}"));
		addCase(new AnalyzerCase("gavamakkunginni", "{gavama:gavama/1n}{kku:kkut/1nn}{nginni:nginni/tn-loc-p-4s}"));
		addCase(new AnalyzerCase("gavamakkunginnit", "{gavama:gavama/1n}{kku:kkut/1nn}{nginnit:nginnit/tn-abl-p-4s}"));
		addCase(new AnalyzerCase("gavamakkunginnut", "{gavama:gavama/1n}{kku:kkut/1nn}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("gavamakkungit", "{gavama:gavama/1n}{kku:kkut/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("gavamakkungita", "{gavama:gavama/1n}{kku:kkut/1nn}{ngita:ngita/tn-gen-p-4s}"));
		addCase(new AnalyzerCase("gavamakkunni", "{gavama:gavama/1n}{kkun:kkut/1nn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("gavamakkunnik", "{gavama:gavama/1n}{kkun:kkut/1nn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("gavamakkunnut", "{gavama:gavama/1n}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("gavamakkut", "{gavama:gavama/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("gavamalirijikkut", "{gavama:gavama/1n}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("gavamalirinirmut", "{gavama:gavama/1n}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("gavamanga", "{gavama:gavama/1n}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("gavamanganut", "{gavama:gavama/1n}{nganut:nganut/tn-dat-s-4s}"));
		addCase(new AnalyzerCase("gavamangata", "{gavama:gavama/1n}{ngata:ngata/tn-gen-s-4s}"));
		addCase(new AnalyzerCase("gavamanginnut", "{gavama:gavama/1n}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("gavamangit", "{gavama:gavama/1n}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("gavamanut", "{gavama:gavama/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("gavamatuqakkunni", "{gavama:gavama/1n}{tuqa:tuqaq/1nn}{kkun:kkut/1nn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("gavamatuqakkunnik", "{gavama:gavama/1n}{tuqa:tuqaq/1nn}{kkun:kkut/1nn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("gavamatuqakkunnit", "{gavama:gavama/1n}{tuqa:tuqaq/1nn}{kkun:kkut/1nn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("gavamatuqakkunnut", "{gavama:gavama/1n}{tuqa:tuqaq/1nn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("gavamatuqakkut", "{gavama:gavama/1n}{tuqa:tuqaq/1nn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("gavamaulluta", "{gavama:gavama/1n}{u:u/1nv}{lluta:luta/tv-part-1p-prespas}"));
		addCase(new AnalyzerCase("gavamaup", "{gavama:gavama/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("gavamavut", "{gavama:gavama/1n}{vut:vut/tn-nom-s-1p}"));
		addCase(new AnalyzerCase("gilin", null)
			.isProperName()
			.comment("Glen"));
		addCase(new AnalyzerCase("guriin", null)
			.isProperName()
			.comment("Green"));
		addCase(new AnalyzerCase("iat", null)
			.isProperName()
			.comment("Ed"));
		addCase(new AnalyzerCase("igluit", "{iglu:iglu/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("iglulirijikkut", "{iglu:iglu/1n}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("iglulirijirjuakkut", "{iglu:iglu/1n}{liri:liri/1nv}{ji:ji/1vn}{rjua:juaq/1nn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("iglulirinirmut", "{iglu:iglu/1n}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("ii", "{ii:ii/1a}"));
		addCase(new AnalyzerCase("iing", null)
			.isProperName()
			.comment("Ng"));
		addCase(new AnalyzerCase("iipuru", "{iipuru:iipuru/1n}"));
		addCase(new AnalyzerCase("iipurul", "{iipurul:iipuru/1n}"));
		addCase(new AnalyzerCase("iituaq", null)
			.isProperName()
			.comment("Edward"));
		addCase(new AnalyzerCase("ikajuqtiit", "{ikajuq:ikajuq/1v}{ti:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ikajuutit", "{ikaju:ikajuq/1v}{ut:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ikkarialuk", "[decomposition:/ikkarialuk(ikkarialuk)/]")
			.isProperName());
		addCase(new AnalyzerCase("ikkarrialuk", "[decomposition:/ikkarrialuk(ikkarrialuk)/]")
			.isProperName());
		addCase(new AnalyzerCase("ikkarrialuuk", "[decomposition:/ikkarrialuuk(ikkarrialuuk)/]")
			.isProperName());
		addCase(new AnalyzerCase("ikpaksaq", "{ikpaksaq:ikpaksaq/1a}"));
		addCase(new AnalyzerCase("iksivauitaaq", "{iksiva:iksiva/1v}{uitaaq:ut/1vn}")
			.isMisspelled());
		addCase(new AnalyzerCase("iksivauta", "{iksiva:iksiva/1v}{uta:ut/1vn}"));
		addCase(new AnalyzerCase("iksivautaa", "{iksiva:iksiva/1v}{uta:ut/1vn}{a:k/tn-nom-d}"));
		addCase(new AnalyzerCase("iksivautaaq", null)
			.possiblyMisspelledWord()
			.comment("TODO-BF: Please add a SHORT comment; [decomposition:/iksiva(iksiva)/utaaq(ut)/]"));
		addCase(new AnalyzerCase("iksivautap", "{iksiva:iksiva/1v}{uta:ut/1vn}{p:up/tn-gen-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("iksivautaq", "{iksiva:iksiva/1v}{utaq:ut/1vn}"));
		addCase(new AnalyzerCase("iksivautaup", "{iksiva:iksiva/1v}{uta:ut/1vn}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("ikummaqqutilirijikkunnut", "{ikummaq:ikummaq/1v}{quti:ut/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}")
			.comment("NT Power Corporation"));
		addCase(new AnalyzerCase("ikupigvilirijikkunnut", "{ikupig:ikupik/1v}{vi:vik/3vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ikupigvilirijikkut", "{ikupig:ikupik/1v}{vi:vik/3vn}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("ikupivvilirijikkunnut", "{ikupiv:ikupik/1v}{vi:vik/3vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ikupivvilirijikkut", "{ikupiv:ikupik/1v}{vi:vik/3vn}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("ilaak", "{ilaak:ilaak/1e}"));
		addCase(new AnalyzerCase("ilaannikkut", "{ilaanni:ilaanni/1a}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("ilaaqai", "{ilaa:ilaak/1e}{qai:qai/1q}"));
		addCase(new AnalyzerCase("ilagiarut", "{ila:ila/1v}{gia:giaq/1vv}{rut:ut/1vn}"));
		addCase(new AnalyzerCase("ilagiaruti", "{ila:ila/1v}{gia:giaq/1vv}{ruti:ut/1vn}"));
		addCase(new AnalyzerCase("ilagiarutiit", "{ila:ila/1v}{gia:giaq/1vv}{ruti:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ilagiarutit", "{ila:ila/1v}{gia:giaq/1vv}{rut:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ilagijaujuq", "{ila:ila/1n}{gi:gi/1nv}{ja:jaq/1vn}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("ilagijaujut", "{ila:ila/1n}{gi:gi/1nv}{ja:jaq/1vn}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("ilanga", "{ila:ila/1n}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("ilangagut", "{ila:ila/1n}{ngagut:ngagut/tn-via-s-4s}"));
		addCase(new AnalyzerCase("ilangani", "{ila:ila/1n}{ngani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("ilangat", "{ila:ila/1n}{ngat:ngat/tn-nom-s-4p}"));
		addCase(new AnalyzerCase("ilangi", "{ila:ila/1n}{ngi:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("ilangillu", "{ila:ila/1n}{ngil:ngit/tn-nom-p-4s}{lu:lu/1q}"));
		addCase(new AnalyzerCase("ilanginni", "{ila:ila/1n}{nginni:nginni/tn-loc-p-4s}"));
		addCase(new AnalyzerCase("ilanginnik", "{ila:ila/1n}{nginnik:nginnik/tn-acc-p-4s}"));
		addCase(new AnalyzerCase("ilanginnit", "{ila:ila/1n}{nginnit:nginnit/tn-abl-p-4s}"));
		addCase(new AnalyzerCase("ilanginnut", "{ila:ila/1n}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("ilangit", "{ila:ila/1n}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("ilinnianirmut", "{ilinnia:ilinniaq/1v}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("ilinniaqtiit", "{ilinniaq:ilinniaq/1v}{ti:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ilinniaqtinik", "{ilinniaq:ilinniaq/1v}{ti:ji/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("ilinniaqtinut", "{ilinniaq:ilinniaq/1v}{ti:ji/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ilinniaqtiujut", "{ilinniaq:ilinniaq/1v}{ti:ji/1vn}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("ilinniaqtulirijikkunnut", "{ilinniaq:ilinniaq/1v}{tu:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ilinniaqtulirijikkut", "{ilinniaq:ilinniaq/1v}{tu:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("ilinniaqtulirinirmut", "{ilinniaq:ilinniaq/1v}{tu:juq/1vn}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("ilinniaqtunut", "{ilinniaq:ilinniaq/1v}{tu:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ilinniaqtut", "{ilinniaq:ilinniaq/1v}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("ilinniarniq", "{ilinniar:ilinniaq/1v}{niq:niq/2vn}"));
		addCase(new AnalyzerCase("ilinniarnirmut", "{ilinniar:ilinniaq/1v}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("ilinniartulirijiit", "{ilinniar:ilinniaq/1v}{tu:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ilinniarviit", "{ilinniar:ilinniaq/1v}{vi:vik/3vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ilinniarvik", "{ilinniar:ilinniaq/1v}{vik:vik/3vn}"));
		addCase(new AnalyzerCase("ilinniarvimmi", "{ilinniar:ilinniaq/1v}{vim:vik/3vn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("ilinniarviup", "{ilinniar:ilinniaq/1v}{vi:vik/3vn}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("iliqqusilirijikkunnut", "{iliqqusi:iliqqusiq/1n}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("iliqqusilirijikkut", "{iliqqusi:iliqqusiq/1n}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("iliqqusilirinirmut", "{iliqqusi:iliqqusiq/1n}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("ilisaijiit", "{ilisaiji:ilisaiji/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ilisaijinut", "{ilisaiji:ilisaiji/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ilitaqsiniq", "{ilitaq:ilitaq/1v}{si:si/3vv}{niq:niq/2vn}"));
		addCase(new AnalyzerCase("ilitarijauningit", "{ilita:ilitaq/1v}{ri:gi/4vv}{ja:jaq/1vn}{u:u/1nv}{ni:niq/2vn}{ngit:ngit/tn-nom-p-4p}"));
		addCase(new AnalyzerCase("illaqtut", "{illaq:iglaq/1v}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("illu", "{illu:iglu/1n}"));
		addCase(new AnalyzerCase("illuit", "{illu:iglu/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("illulirijikkut", "{illu:iglu/1n}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("illulirijirjuakkut", "{illu:iglu/1n}{liri:liri/1nv}{ji:ji/1vn}{rjua:juaq/1nn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("illulirinirmut", "{illu:iglu/1n}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("illumi", "{illu:iglu/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("illumik", "{illu:iglu/1n}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("illumut", "{illu:iglu/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("illuni", "{illu:iglu/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("illunik", "{illu:iglu/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("illunut", "{illu:iglu/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("iluagut", "{ilu:ilu/1n}{agut:ngagut/tn-via-s-4s}"));
		addCase(new AnalyzerCase("iluani", "{ilu:ilu/1n}{ani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("ilulingit", "{ilu:ilu/1n}{li:lik/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("ilulingita", "{ilu:ilu/1n}{li:lik/1nn}{ngita:ngita/tn-gen-p-4s}"));
		addCase(new AnalyzerCase("iluunnatik", "{iluunnatik:iluunnatik/1p}"));
		addCase(new AnalyzerCase("imaak", "{imaak:imaak/1a}"));
		addCase(new AnalyzerCase("imaimmat", null)
			.possiblyMisspelledWord()
			.comment("TODO-BF: Please add a SHORT comment; [decomposition:/imaimmat(imaimmat)/]"));
		addCase(new AnalyzerCase("imanna", "{imanna:imannak/1a}"));
		addCase(new AnalyzerCase("imannak", "{imannak:imannak/1a}"));
		addCase(new AnalyzerCase("immagaa", "{immagaa:immaqaa/1a}")
			.isMisspelled());
		addCase(new AnalyzerCase("immaqa", "{immaqa:immaqaa/1a}")
			.isMisspelled());
		addCase(new AnalyzerCase("imminik", "{imminik:imminik/1a}"));
		addCase(new AnalyzerCase("ing", null)
			.isProperName()
			.comment("Ng"));
		addCase(new AnalyzerCase("ingirrajulirijikkunnut", "{ingirra:ingirra/1v}{ju:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ingirrajulirijikkut", "{ingirra:ingirra/1v}{ju:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("ingirrajulirinirmut", "{ingirra:ingirra/1v}{ju:juq/1vn}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("innatuqait", "{inna:innaq/1n}{tuqa:tuqaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("innatuqarnik", "{inna:innaq/1n}{tuqar:tuqaq/1nn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("innatuqarnut", "{inna:innaq/1n}{tuqar:tuqaq/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("innirvik", "[decomposition:/innirvik(innirvik)/]")
			.isProperName());
		addCase(new AnalyzerCase("inuinnaqtun", "[decomposition:/inuinnaqtun(inuinnaqtun)/]")
			.correctDecompUnknown());
		addCase(new AnalyzerCase("inuit", "{inu:inuk/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("inuk", "{inuk:inuk/1n}"));
		addCase(new AnalyzerCase("inuki", "[decomposition:/inuki(inuki)/]")
			.isProperName());
		addCase(new AnalyzerCase("inuktitut", "{inuk:inuk/1n}{titut:titut/tn-sim-p}"));
		addCase(new AnalyzerCase("inulimaanut", "{inu:inuk/1n}{limaa:limaaq/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("inulirijikkullu", "{inu:inuk/1n}{liri:liri/1nv}{ji:ji/1vn}{kkul:kkut/1nn}{lu:lu/1q}"));
		addCase(new AnalyzerCase("inulirijikkunnut", "{inu:inuk/1n}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("inulirijikkut", "{inu:inuk/1n}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("inulirinirmut", "{inu:inuk/1n}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("inunginnut", "{inu:inuk/1n}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("inungit", "{inu:inuk/1n}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("inungni", "{inung:inuk/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("inungnut", "{inung:inuk/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("inunni", "{inun:inuk/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("inunnik", "{inun:inuk/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("inunnit", "{inun:inuk/1n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("inunnu", "{inun:inuk/1n}{nu:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("inunnut", "{inun:inuk/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("inuqutivut", "{inu:inuk/1n}{quti:quti/1nn}{vut:vut/tn-nom-s-1p}"));
		addCase(new AnalyzerCase("inuttitut", "{inut:inuk/1n}{titut:titut/tn-sim-p}"));
		addCase(new AnalyzerCase("inutuqait", "{inu:inuk/1n}{tuqa:tuqaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("inuusilirijikkunullu", "{inu:inuk/1n}{u:u/1nv}{si:siq/2vn}{liri:liri/1nv}{ji:ji/1vn}{kku:kkut/1nn}{nul:nut/tn-dat-p}{lu:lu/1q}"));
		addCase(new AnalyzerCase("ippaksaq", "{ippaksaq:ikpaksaq/1a}"));
		addCase(new AnalyzerCase("ippassaq", "{ippassaq:ikpaksaq/1a}"));
		addCase(new AnalyzerCase("ippatsaq", "{ippatsaq:ikpaksaq/1a}"));
		addCase(new AnalyzerCase("iqaluit", "{iqalu:iqaluk/2n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("iqaluit", "{iqaluit:iqaluit/1n}"));
		addCase(new AnalyzerCase("iqalungni", "{iqalung:iqaluk/2n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("iqalunni", "{iqalun:iqaluk/2n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("iqalunnit", "{iqalun:iqaluk/2n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("iqalunnut", "{iqalun:iqaluk/2n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("iqaluttuuttiaq", "{iqaluttuuttiaq:iqaluktuutsiaq/1n}"));
		addCase(new AnalyzerCase("iqittuq", "[decomposition:/iqittuq(iqittuq)/]")
			.isProperName());
		addCase(new AnalyzerCase("iqqanaijaat", "{iqqanaijaa:iqqanaijaaq/1n}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("iqqanaijaqtiit", "{iqqanaijaq:iqqanaijaq/1v}{ti:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("iqqanaijaqtikka", "{iqqanaijaq:iqqanaijaq/1v}{ti:ji/1vn}{kka:kka/tn-nom-p-1s}"));
		addCase(new AnalyzerCase("iqqanaijaqtinginnut", "{iqqanaijaq:iqqanaijaq/1v}{ti:ji/1vn}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("iqqanaijaqtingit", "{iqqanaijaq:iqqanaijaq/1v}{ti:ji/1vn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("iqqanaijaqtinik", "{iqqanaijaq:iqqanaijaq/1v}{ti:ji/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("iqqanaijaqtinit", "{iqqanaijaq:iqqanaijaq/1v}{ti:ji/1vn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("iqqanaijaqtinut", "{iqqanaijaq:iqqanaijaq/1v}{ti:ji/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("iqqanaijaqtiujut", "{iqqanaijaq:iqqanaijaq/1v}{ti:ji/1vn}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("iqqanaijaqtulirijikkunnit", "{iqqanaijaq:iqqanaijaq/1v}{tu:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("iqqanaijaqtulirijikkut", "{iqqanaijaq:iqqanaijaq/1v}{tu:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("iqqaqtuivilirijikkunnut", "{iqqaq:iqqaq/1v}{tu:tuq/1vv}{i:i/1vv}{vi:vik/3vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("iqqaqtuivilirijikkut", "{iqqaq:iqqaq/1v}{tu:tuq/1vv}{i:i/1vv}{vi:vik/3vn}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("iqqittuq", "[decomposition:/iqqittuq(iqqittuq)/]")
			.isProperName());
		addCase(new AnalyzerCase("issivauta", "{issiva:iksiva/1v}{uta:ut/1vn}"));
		addCase(new AnalyzerCase("issivautaa", "{issiva:iksiva/1v}{uta:ut/1vn}{a:k/tn-nom-d}"));
		addCase(new AnalyzerCase("issivautaaq", "")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("issivautaq", "{issiva:iksiva/1v}{utaq:ut/1vn}"));
		addCase(new AnalyzerCase("isumagillugu", "{isuma:isuma/1n}{gi:gi/1nv}{llugu:lugu/tv-part-1s-3s-prespas}"));
		addCase(new AnalyzerCase("itsivauta", "{itsiva:iksiva/1v}{uta:ut/1vn}"));
		addCase(new AnalyzerCase("itsivautaa", "{itsiva:iksiva/1v}{uta:ut/1vn}{a:k/tn-nom-d}"));
		addCase(new AnalyzerCase("itsivautaaq", null)
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; [decomposition:/itsiva(iksiva)/utaaq(ut)/]"));
		addCase(new AnalyzerCase("itsivautaq", "{itsiva:iksiva/1v}{utaq:ut/1vn}"));
		addCase(new AnalyzerCase("ivvit", "{ivvit:igvit/1p}"));
		addCase(new AnalyzerCase("jaak", "[decomposition:/jaak(jaak)/]")
			.isProperName());
		addCase(new AnalyzerCase("jaan", "[decomposition:/jaan(jaan)/]")
			.isProperName());
		addCase(new AnalyzerCase("jaimisi", "[decomposition:/jaimisi(jaimisi)/]")
			.isProperName());
		addCase(new AnalyzerCase("jaims", "[decomposition:/jaims(jaims)/]")
			.isProperName());
		addCase(new AnalyzerCase("julai", "{julai:julai/1n}"));
		addCase(new AnalyzerCase("juraias", "[decomposition:/juraias(juraias)/]")
			.isProperName());
		addCase(new AnalyzerCase("juupi", "[decomposition:/juupi(juupi)/]")
			.isProperName());
		addCase(new AnalyzerCase("kaalvin", "[decomposition:/kaalvin(kaalvin)/]")
			.isProperName());
		addCase(new AnalyzerCase("kajusijuq", "{kajusi:kajusi/1v}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("kajusivuq", "{kajusi:kajusi/1v}{vuq:vuq/tv-dec-3s}"));
		addCase(new AnalyzerCase("kamagijalik", "{kama:kama/1v}{gi:gi/4vv}{ja:jaq/1vn}{lik:lik/1nn}"));
		addCase(new AnalyzerCase("kamajuq", "{kama:kama/1v}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("kamisana", "{kamisana:kamisina/1n}"));
		addCase(new AnalyzerCase("kamisina", "{kamisina:kamisina/1n}"));
		addCase(new AnalyzerCase("kamisinaup", "{kamisina:kamisina/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("kanannangani", "{kananna:kanangnaq/1n}{ngani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("kanata", "{kanata:kanata/1n}"));
		addCase(new AnalyzerCase("kanatalimaami", "{kanata:kanata/1n}{limaa:limaaq/1nn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("kanatami", "{kanata:kanata/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("kanataup", "{kanata:kanata/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("kangiqtugaapimmi", "{kangiqtugaapim:kangiqtugaapik/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("katilimaaqtugit", "{kati:kati/1v}{limaaq:limaaq/2vv}{tugit:lugit/tv-part-1s-3p-prespas}"));
		addCase(new AnalyzerCase("katillugit", "{kati:kati/1v}{llugit:lugit/tv-part-1s-3p-prespas}"));
		addCase(new AnalyzerCase("katimaji", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}"));
		addCase(new AnalyzerCase("katimajiit", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("katimajilimaat", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{limaa:limaaq/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("katimajingannit", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ngannit:ngannit/tn-abl-s-4d}"));
		addCase(new AnalyzerCase("katimajinginnik", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{nginnik:nginnik/tn-acc-p-4s}"));
		addCase(new AnalyzerCase("katimajinginnit", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{nginnit:nginnit/tn-abl-p-4s}"));
		addCase(new AnalyzerCase("katimajinginnut", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("katimajingit", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("katimajingita", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ngita:ngita/tn-gen-p-4s}"));
		addCase(new AnalyzerCase("katimajinut", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("katimajiralaa", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ralaa:ralaaq/1nn}"));
		addCase(new AnalyzerCase("katimajiralaangit", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ralaa:ralaaq/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("katimajiralaanguinnaqtut", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ralaa:ralaaq/1nn}{ngu:u/1nv}{innaq:innaq/2vv}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("katimajiralaangukainnaqtunut", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ralaa:ralaaq/1nn}{ngu:u/1nv}{kainnaq:kainnaq/1vv}{tu:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("katimajiralaani", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ralaa:ralaaq/1nn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("katimajiralaanit", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ralaa:ralaaq/1nn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("katimajiralaanut", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ralaa:ralaaq/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("katimajiralaat", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{ralaa:ralaaq/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("katimajit", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{t:it/tn-nom-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment"));
		addCase(new AnalyzerCase("katimajiujut", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("katimajiuqataujut", "{kati:kati/1v}{ma:ma/1vv}{ji:ji/1vn}{u:u/1nv}{qatau:qatau/1vv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("katimajjutiksait", "{kati:kati/1v}{ma:ma/1vv}{jjuti:jjut/1vn}{ksa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("katimajjutiksanut", "{kati:kati/1v}{ma:ma/1vv}{jjuti:jjut/1vn}{ksa:ksaq/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("katimajjutiksaq", "{kati:kati/1v}{ma:ma/1vv}{jjuti:jjut/1vn}{ksaq:ksaq/1nn}"));
		addCase(new AnalyzerCase("katimajjutissaq", "{kati:kati/1v}{ma:ma/1vv}{jjuti:jjut/1vn}{ssaq:ksaq/1nn}"));
		addCase(new AnalyzerCase("katimaniq", "{kati:kati/1v}{ma:ma/1vv}{niq:niq/2vn}"));
		addCase(new AnalyzerCase("katimatillugit", "{kati:kati/1v}{ma:ma/1vv}{tillugit:tillugit/tv-part-3p}"));
		addCase(new AnalyzerCase("katimatuinnaqtillugit", "{kati:kati/1v}{ma:ma/1vv}{tuinnaq:tuinnaq/1vv}{tillugit:tillugit/tv-part-3p}"));
		addCase(new AnalyzerCase("katimautiminingit", "{kati:kati/1v}{ma:ma/1vv}{uti:ut/1vn}{mini:miniq/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("katimautinik", "{kati:kati/1v}{ma:ma/1vv}{uti:ut/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("katimautitsa", "{kati:kati/1v}{ma:ma/1vv}{uti:ut/1vn}{tsa:ksaq/1nn}"));
		addCase(new AnalyzerCase("katimavimmi", "{kati:kati/1v}{ma:ma/1vv}{vim:vik/3vn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("katimmajjutiksaq", "{kati:kati/1v}{mma:ma/1v}{jjuti:jjut/1vn}{ksaq:ksaq/1nn}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment;"));
		addCase(new AnalyzerCase("katitsutik", "{katit:katit/1v}{sutik:lutik/tv-part-3p-prespas}"));
		addCase(new AnalyzerCase("katittugit", "{katit:katit/1v}{tugit:lugit/tv-part-4p-3p-prespas}"));
		addCase(new AnalyzerCase("kattuk", "[decomposition:/kattuk(kattuk)/]")
			.isProperName());
		addCase(new AnalyzerCase("kattuq", "[decomposition:/kattuq(kattuq)/]")
			.isProperName());
		addCase(new AnalyzerCase("katujjiqatigiingit", "{katujji:katujji/1v}{qati:qati/1vn}{gii:giik/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("katujjiqatigiit", "{katujji:katujji/1v}{qati:qati/1vn}{gii:giik/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("kialvin", "[decomposition:/kialvin(kialvin)/]")
			.isProperName());
		addCase(new AnalyzerCase("kiinaujait", "{kiinauja:kiinaujaq/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("kiinaujalirijiit", "{kiinauja:kiinaujaq/1n}{liri:liri/1nv}{ji:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("kiinaujalirijikkunnut", "{kiinauja:kiinaujaq/1n}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("kiinaujalirijikkut", "{kiinauja:kiinaujaq/1n}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("kiinaujalirinirmut", "{kiinauja:kiinaujaq/1n}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("kiinaujani", "{kiinauja:kiinaujaq/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("kiinaujanik", "{kiinauja:kiinaujaq/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("kiinaujanit", "{kiinauja:kiinaujaq/1n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("kiinaujanut", "{kiinauja:kiinaujaq/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("kiinaujatigut", "{kiinauja:kiinaujaq/1n}{tigut:tigut/tn-via-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment;"));
		addCase(new AnalyzerCase("kikkulimaanut", "{kikku:kikkut/1p}{limaa:limaaq/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("kikkulimaat", "{kikku:kikkut/1p}{limaa:limaaq/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("kikkut", "{kikkut:kikkut/1p}"));
		addCase(new AnalyzerCase("kikkutuinnait", "{kikku:kikkut/1p}{tuinna:tuinnaq/2nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("kikkutuinnarnik", "{kikku:kikkut/1p}{tuinnar:tuinnaq/2nn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("kikkutuinnarnut", "{kikku:kikkut/1p}{tuinnar:tuinnaq/2nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("kina", "{kina:kina/1p}"));
		addCase(new AnalyzerCase("kinatuinnaq", "{kina:kina/1p}{tuinnaq:tuinnaq/2nn}"));
		addCase(new AnalyzerCase("kingulliqpaami", "{kingu:kingu/1n}{lliq:&iq/1nn}{paa:paaq/1nn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("kingulliqpaamik", "{kingu:kingu/1n}{lliq:&iq/1nn}{paa:paaq/1nn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("kingulliqpaamit", "{kingu:kingu/1n}{lliq:&iq/1nn}{paa:paaq/1nn}{mit:mit/tn-abl-s}"));
		addCase(new AnalyzerCase("kingulliqpaaq", "{kingu:kingu/1n}{lliq:&iq/1nn}{paaq:paaq/1nn}"));
		addCase(new AnalyzerCase("kingullirmi", "{kingu:kingu/1n}{llir:&iq/1nn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("kinguniagut", "{kinguni:kinguni/1n}{agut:ngagut/tn-via-s-4s}"));
		addCase(new AnalyzerCase("kingunittinni", "{kinguni:kinguni/1n}{ttinni:ttinni/tn-loc-s-1d}"));
		addCase(new AnalyzerCase("kinngarni", "{kinngar:kinngait/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("kisiani", "{kisiani:kisiani/1c}"));
		addCase(new AnalyzerCase("kisianili", "{kisiani:kisiani/1c}{li:li/1q}"));
		addCase(new AnalyzerCase("kisianittauq", "{kisiani:kisiani/1c}{ttauq:ttauq/1q}"));
		addCase(new AnalyzerCase("kisimi", "{kisimi:kisimi/1c}"));
		addCase(new AnalyzerCase("kisimili", "{kisimi:kisimi/1c}{li:li/1q}"));
		addCase(new AnalyzerCase("kisumut", "{kisu:kisu/1p}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("kisunik", "{kisu:kisu/1p}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("kisut", "{kisut:kisut/1p}"));
		addCase(new AnalyzerCase("kisutuinnait", "{kisutuinna:kisutuinnaq/1p}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("kiujjutiit", "{kiu:kiu/1v}{jjuti:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("kiujjutinga", "{kiu:kiu/1v}{jjuti:ut/1vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("kiujjutit", "{kiu:kiu/1v}{jjut:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("kiulvan", "[decomposition:/kiulvan(kiulvan)/]")
			.isProperName());
		addCase(new AnalyzerCase("kiulvin", "[decomposition:/kiulvin(kiulvin)/]")
			.isProperName());
		addCase(new AnalyzerCase("kiuvan", "[decomposition:/kiuvan(kiuvan)/]")
			.isProperName());
		addCase(new AnalyzerCase("kivallirmi", "{kivallir:kivalliq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("kivallirmut", "{kivallir:kivalliq/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("kivan", "[decomposition:/kivan(kivan)/]")
			.isProperName());
		addCase(new AnalyzerCase("kivin", "[decomposition:/kivin(kivin)/]")
			.isProperName());
		addCase(new AnalyzerCase("kuak", "[decomposition:/kuak(kuak)/]")
			.isProperName());
		addCase(new AnalyzerCase("kuaq", "[decomposition:/kuaq(kuaq)/]")
			.isProperName());
		addCase(new AnalyzerCase("kuupa", null)
			.isProperName()
			.comment("Cooper"));
		addCase(new AnalyzerCase("liivai", "[decomposition:/liivai(liivai)/]")
			.isProperName());
		addCase(new AnalyzerCase("livai", "[decomposition:/livai(livai)/]")
			.isProperName());
		addCase(new AnalyzerCase("na", "[decomposition:/na(na)/]")
			.correctDecompUnknown());
		addCase(new AnalyzerCase("maajji", "{maajji:maatsi/1n}"));
		addCase(new AnalyzerCase("maani", "{ma:ma/rad-ml}{ani:ani/tad-loc}"));
		addCase(new AnalyzerCase("maanna", "{maanna:maanna/1a}"));
		addCase(new AnalyzerCase("maannakkut", "{maanna:maanna/1a}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("maannali", "{maanna:maanna/1a}{li:li/1q}"));
		addCase(new AnalyzerCase("maannamut", "{maanna:maanna/1a}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("maannauju", "{maanna:maanna/1a}{u:u/1nv}{ju:juq/1vn}"));
		addCase(new AnalyzerCase("maannaujukkut", "{maanna:maanna/1a}{u:u/1nv}{ju:juq/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("maannaujumi", "{maanna:maanna/1a}{u:u/1nv}{ju:juq/1vn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("maannaujuq", "{maanna:maanna/1a}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("maatsi", "{maatsi:maatsi/1n}"));
		addCase(new AnalyzerCase("mai", "{mai:mai/1e}"));
		addCase(new AnalyzerCase("maik", "[decomposition:/maik(maik)/]")
			.isProperName());
		addCase(new AnalyzerCase("makkuktunut", "{makkuk:makkuk/1v}{tu:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("makkuktut", "{makkuk:makkuk/1v}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("makkuttuit", "{makkut:makkuk/1v}{tu:juq/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("makkuttunut", "{makkut:makkuk/1v}{tu:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("maklain", "[decomposition:/maklain(maklain)/]")
			.isProperName());
		addCase(new AnalyzerCase("makliin", "[decomposition:/makliin(makliin)/]")
			.isProperName());
		addCase(new AnalyzerCase("makpigaq", "{makpi:makpiq/1v}{gaq:gaq/1vn}"));
		addCase(new AnalyzerCase("makua", "{makua:makua/pd-ml-p}"));
		addCase(new AnalyzerCase("maligait", "{mali:malik/1v}{ga:gaq/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("maligaksait", "{mali:malik/1v}{ga:gaq/1vn}{ksa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("maligaksanit", "{mali:malik/1v}{ga:gaq/1vn}{ksa:ksaq/1nn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("maligaksaq", "{mali:malik/1v}{ga:gaq/1vn}{ksaq:ksaq/1nn}"));
		addCase(new AnalyzerCase("maligaliriji", "{mali:malik/1v}{ga:gaq/1vn}{liri:liri/1nv}{ji:ji/1vn}"));
		addCase(new AnalyzerCase("maligalirinirmut", "{mali:malik/1v}{ga:gaq/1vn}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("maligaliuqti", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}"));
		addCase(new AnalyzerCase("maligaliuqtii", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{i:it/tn-nom-p}"));
		addCase(new AnalyzerCase("maligaliuqtiit", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("maligaliuqtikkut", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("maligaliuqtilimaanut", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{limaa:limaaq/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("maligaliuqtilimaat", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{limaa:limaaq/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("maligaliuqtimut", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("maligaliuqtinik", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("maligaliuqtinit", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("maligaliuqtinut", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("maligaliuqtit", "{maliga:maligaq/1n}{liuq:liuq/1nv}{ti:ji/1vn}{t:it/tn-nom-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("maligaliuqtiujuq", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("maligaliuqtiujut", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("maligaliuqtiup", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("maligaliuqtiuqatiga", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{u:u/1nv}{qati:qati/1vn}{ga:ga/tn-nom-s-1s}"));
		addCase(new AnalyzerCase("maligaliuqtiuqatikka", "{mali:malik/1v}{ga:gaq/1vn}{liuq:liuq/1nv}{ti:ji/1vn}{u:u/1nv}{qati:qati/1vn}{kka:kka/tn-nom-p-1s}"));
		addCase(new AnalyzerCase("maligaliurti", "{mali:malik/1v}{ga:gaq/1vn}{liur:liuq/1nv}{ti:ji/1vn}"));
		addCase(new AnalyzerCase("maligaliurtiit", "{mali:malik/1v}{ga:gaq/1vn}{liur:liuq/1nv}{ti:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("maligaliurtiup", "{mali:malik/1v}{ga:gaq/1vn}{liur:liuq/1nv}{ti:ji/1vn}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("maligaliurvik", "{maligaliurvik:maligaliurvik/1n}"));
		addCase(new AnalyzerCase("maligaliurviliarsimajut", "{maligaliurvi:maligaliurvik/1n}{liar:liaq/2nv}{sima:sima/1vv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("maligaliurvimmi", "{maligaliurvim:maligaliurvik/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("maligaliurvimmit", "{maligaliurvim:maligaliurvik/1n}{mit:mit/tn-abl-s}"));
		addCase(new AnalyzerCase("maligaliurvimmut", "{maligaliurvim:maligaliurvik/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("maligaliurvinga", "{maligaliurvi:maligaliurvik/1n}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("maligaliurvingmi", "{maligaliurving:maligaliurvik/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("maligaliurvingmut", "{maligaliurving:maligaliurvik/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("maligaliurviup", "{maligaliurvi:maligaliurvik/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("maliganga", "{mali:malik/1v}{ga:gaq/1vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("maligaq", "{mali:malik/1v}{gaq:gaq/1vn}"));
		addCase(new AnalyzerCase("maligarmi", "{mali:malik/1v}{gar:gaq/1vn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("maligarmik", "{mali:malik/1v}{gar:gaq/1vn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("maligarmut", "{mali:malik/1v}{gar:gaq/1vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("maligarnik", "{mali:malik/1v}{gar:gaq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("maligarnit", "{mali:malik/1v}{gar:gaq/1vn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("maligassait", "{mali:malik/1v}{ga:gaq/1vn}{ssa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("maligassaq", "{mali:malik/1v}{ga:gaq/1vn}{ssaq:ksaq/1nn}"));
		addCase(new AnalyzerCase("maligatigut", "{maliga:maligaq/1n}{tigut:tigut/tn-via-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("maligatsait", "{mali:malik/1v}{ga:gaq/1vn}{tsa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("maligatsaq", "{mali:malik/1v}{ga:gaq/1vn}{tsaq:ksaq/1nn}"));
		addCase(new AnalyzerCase("maligaujuq", "{mali:malik/1v}{ga:gaq/1vn}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("maligaup", "{mali:malik/1v}{ga:gaq/1vn}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("maliglugit", "{malig:malik/1v}{lugit:lugit/tv-part-1s-3p-fut}"));
		addCase(new AnalyzerCase("maliktugit", "{malik:malik/1v}{tugit:lugit/tv-part-1s-3p-prespas}"));
		addCase(new AnalyzerCase("malillugit", "{malil:malik/1v}{lugit:lugit/tv-part-1s-3p-fut}"));
		addCase(new AnalyzerCase("malillugu", "{malil:malik/1v}{lugu:lugu/tv-part-1s-3s-fut}"));
		addCase(new AnalyzerCase("malittugit", "{malit:malik/1v}{tugit:lugit/tv-part-1s-3p-prespas}"));
		addCase(new AnalyzerCase("malittugu", "{malit:malik/1v}{tugu:lugu/tv-part-1s-3s-prespas}"));
		addCase(new AnalyzerCase("mamianaq", "{mamia:mamiak/1v}{naq:naq/1vn}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("mamiappunga", "{mamiap:mamiak/1v}{punga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("maniittuq", "[decomposition:/maniittuq(maniittuq)/]")
			.isProperName());
		addCase(new AnalyzerCase("manna", "{manna:manna/pd-ml-s}"));
		addCase(new AnalyzerCase("mannaujuq", "{manna:manna/pd-ml-s}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("mappigaq", "{mappi:makpiq/1v}{gaq:gaq/1vn}"));
		addCase(new AnalyzerCase("mappiqtugaq", "{mappiq:makpiq/1v}{tu:tuq/1vv}{gaq:gaq/1vn}"));
		addCase(new AnalyzerCase("marrunnik", "{marrun:marruuk/1n}{nik:nik/tn-acc-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("marruuk", "{marruuk:marruuk/1n}"));
		addCase(new AnalyzerCase("marruunni", "{marruun:marruuk/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("marruunnik", "{marruun:marruuk/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("mataam", "{mataam:mataam/1n}"));
		addCase(new AnalyzerCase("matuiqsinirmut", "{matu:matu/1n}{iq:iq/1nv}{si:si/2vv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("matuiqtauninga", "{matu:matu/1n}{iq:iq/1nv}{ta:jaq/1vn}{u:u/1nv}{ni:niq/2vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("matuirutimut", "{matu:matu/1n}{i:iq/1nv}{ruti:ut/1vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("miksaanut", "{miksa:miksa/1n}{anut:nganut/tn-dat-s-4s}"));
		addCase(new AnalyzerCase("milian", "{milian:miliat/1n}"));
		addCase(new AnalyzerCase("milianik", "{milia:milian/1n}{nik:nik/tn-acc-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("milianit", "{milia:milian/1n}{nit:nit/tn-abl-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("minisitaujuq", "{minisita:minista/1n}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("minista", "{minista:minista/1n}"));
		addCase(new AnalyzerCase("ministaa", "{minista:minista/1n}{a:k/tn-nom-d}"));
		addCase(new AnalyzerCase("ministait", "{minista:minista/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ministamik", "{minista:minista/1n}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("ministamut", "{minista:minista/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("ministanga", "{minista:minista/1n}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("ministangannut", "{minista:minista/1n}{ngannut:ngannut/tn-dat-s-4d}"));
		addCase(new AnalyzerCase("ministangat", "{minista:minista/1n}{ngat:ngat/tn-nom-s-4p}"));
		addCase(new AnalyzerCase("ministanut", "{minista:minista/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ministau", "{minista:minista/1n}{u:up/tn-gen-s}"));
		addCase(new AnalyzerCase("ministaujuq", "{minista:minista/1n}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("ministaujut", "{minista:minista/1n}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("ministaullunga", "{minista:minista/1n}{u:u/1nv}{llunga:lunga/tv-part-1s-prespas}"));
		addCase(new AnalyzerCase("ministaup", "{minista:minista/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("mis", "{mis:mis/1n}"));
		addCase(new AnalyzerCase("missaanut", "{missa:miksa/1n}{anut:nganut/tn-dat-s-4s}"));
		addCase(new AnalyzerCase("mista", "{mista:mista/1n}"));
		addCase(new AnalyzerCase("mistu", "{mistu:mista/1n}"));
		addCase(new AnalyzerCase("mitsaanut", "{mitsa:miksa/1n}{anut:nganut/tn-dat-s-4s}"));
		addCase(new AnalyzerCase("mittimatalimmi", "{mittimatalim:mittimatalik/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("mittimatalingmi", "{mittimataling:mittimatalik/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("naalautikkut", "{naala:naalak/1v}{uti:ut/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("naammappuq", "{naammap:naamak/1v}{puq:vuq/tv-dec-3s}"));
		addCase(new AnalyzerCase("naammasaqtuit", "{naamma:naamak/1v}{saq:ksaq/2vv}{tu:juq/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("naammasaqtut", "{naamma:naamak/1v}{saq:ksaq/2vv}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("naansi", "[decomposition:/naansi(naansi)/]")
			.isProperName());
		addCase(new AnalyzerCase("naasautaa", "{naasa:naasaq/1v}{uta:ut/1vn}{a:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("naasautiit", "{naasa:naasaq/1v}{uti:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("naasautik", "{naasa:naasaq/1v}{utik:ut/1vn}"));
		addCase(new AnalyzerCase("naasautilik", "{naasa:naasaq/1v}{uti:ut/1vn}{lik:lik/1nn}"));
		addCase(new AnalyzerCase("nakit", "{nakit:nakit/1a}"));
		addCase(new AnalyzerCase("nakuqmi", "{nakuq:nakuq/1v}{mi:miik/1vn}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("nakuqmii", "{nakuq:nakuq/1v}{mii:miik/1vn}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("nakurmii", "{nakur:nakuq/1v}{mii:miik/1vn}"));
		addCase(new AnalyzerCase("nakurmiik", "{nakur:nakuq/1v}{miik:miik/1vn}"));
		addCase(new AnalyzerCase("naliak", "{naliak:naliak/1p}"));
		addCase(new AnalyzerCase("naliqqangit", "{naliqqa:naliqqaq/1n}{ngit:ngit/tn-nom-p-4s}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("nalliani", "{nalli:nalliq/1p}{ani:ngani/tn-loc-s-4s}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("nalunanngittuq", "{naluna:nalunak/1v}{nngit:nngit/1vv}{tuq:juq/1vn}"));
		addCase(new AnalyzerCase("namminiq", "{namminiq:nangminiq/1n}"));
		addCase(new AnalyzerCase("namminiqaqtunut", "{nammini:nangminiq/1n}{qaq:qaq/1nv}{tu:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("nangiqsivunga", "{nangiq:nangiq/1v}{si:si/2vv}{vunga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("nangminiq", "{nangminiq:nangminiq/1n}"));
		addCase(new AnalyzerCase("nanuit", "{nanu:nanuq/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("nanulik", "{nanu:nanuq/1n}{lik:lik/1nn}"));
		addCase(new AnalyzerCase("natsilik", "{natsilik:nattilik/1n}"));
		addCase(new AnalyzerCase("naujaani", "{naujaa:naujaat/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("naukkut", "{nau:nauk/1a}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("nigiani", "{nigi:niggig/1n}{ani:ngani/tn-loc-s-4s}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("nikuvippunga", "{nikuvip:nikuvik/1v}{punga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("niqsunaqtuq", "{niqsu:niqtuq/1v}{naq:naq/1vv}{tuq:juq/1vn}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("niqtunaqtuq", "{niqtu:niqtuq/1v}{naq:naq/1vv}{tuq:juq/1vn}"));
		addCase(new AnalyzerCase("niriuppugut", "{niriup:niriuk/1v}{pugut:vugut/tv-dec-1p}"));
		addCase(new AnalyzerCase("niriuppunga", "{niriup:niriuk/1v}{punga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("nirtunartuq", "{nirtu:niqtuq/1v}{nar:naq/1vv}{tuq:juq/1vn}"));
		addCase(new AnalyzerCase("niruaqtulirinirmut", "{niruaq:niruaq/1v}{tu:juq/1vn}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("nunaliit", "{nuna:nuna/1n}{li:lik/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("nunalilimaat", "{nuna:nuna/1n}{li:lik/1nn}{limaa:limaaq/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("nunalinginni", "{nuna:nuna/1n}{li:lik/1nn}{nginni:nginni/tn-loc-p-4s}"));
		addCase(new AnalyzerCase("nunalingni", "{nuna:nuna/1n}{ling:lik/1nn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("nunalingnit", "{nuna:nuna/1n}{ling:lik/1nn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("nunalingnut", "{nuna:nuna/1n}{ling:lik/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("nunalinni", "{nuna:nuna/1n}{lin:lik/1nn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("nunalinnik", "{nuna:nuna/1n}{lin:lik/1nn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("nunalinnit", "{nuna:nuna/1n}{lin:lik/1nn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("nunalinnu", "{nuna:nuna/1n}{lin:lik/1nn}{nu:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("nunalinnut", "{nuna:nuna/1n}{lin:lik/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("nunalittinni", "{nuna:nuna/1n}{li:lik/1nn}{ttinni:ttinni/tn-loc-s-1d}"));
		addCase(new AnalyzerCase("nunaliujuni", "{nuna:nuna/1n}{li:lik/1nn}{u:u/1nv}{ju:juq/1vn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("nunaliujunik", "{nuna:nuna/1n}{li:lik/1nn}{u:u/1nv}{ju:juq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("nunaliujunit", "{nuna:nuna/1n}{li:lik/1nn}{u:u/1nv}{ju:juq/1vn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("nunaliujunut", "{nuna:nuna/1n}{li:lik/1nn}{u:u/1nv}{ju:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("nunaliujuq", "{nuna:nuna/1n}{li:lik/1nn}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("nunaliujut", "{nuna:nuna/1n}{li:lik/1nn}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("nunami", "{nuna:nuna/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("nunanganni", "{nuna:nuna/1n}{nganni:nganni/tn-loc-s-4d}"));
		addCase(new AnalyzerCase("nunangannut", "{nuna:nuna/1n}{ngannut:ngannut/tn-dat-s-4d}"));
		addCase(new AnalyzerCase("nunatsiap", "{nunatsia:nunatsiaq/1n}{p:up/tn-gen-s}"));
		addCase(new AnalyzerCase("nunatsiarmi", "{nunatsiar:nunatsiaq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("nunattiap", "{nunattia:nunatsiaq/1n}{p:up/tn-gen-s}"));
		addCase(new AnalyzerCase("nunattinni", "{nuna:nuna/1n}{ttinni:ttinni/tn-loc-s-1d}"));
		addCase(new AnalyzerCase("nunavu", "{nunavu:nunavut/1n}"));
		addCase(new AnalyzerCase("nunavulimaami", "{nunavu:nunavut/1n}{limaa:limaaq/1nn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("nunavumi", "{nunavu:nunavut/1n}{mi:mi/tn-loc-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("nunavumiunut", "{nunavu:nunavut/1n}{miu:miuq/1nn}{nut:nut/tn-dat-p}")
			.isMisspelled());
		addCase(new AnalyzerCase("nunavumiut", "{nunavu:nunavut/1n}{miu:miuq/1nn}{t:it/tn-nom-p}")
			.isMisspelled());
		addCase(new AnalyzerCase("nunavummi", "{nunavum:nunavut/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("nunavummiunut", "{nunavum:nunavut/1n}{miu:miuq/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("nunavummiut", "{nunavum:nunavut/1n}{miu:miuq/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("nunavummut", "{nunavum:nunavut/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("nunavumut", "{nunavu:nunavut/1n}{mut:mut/tn-dat-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("nunavungmi", "{nunavung:nunavut/1n}{mi:mi/tn-loc-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("nunavut", "{nunavut:nunavut/1n}"));
		addCase(new AnalyzerCase("nunavutmi", "{nunavut:nunavut/1n}{mi:mi/tn-loc-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("nunavuumi", "{nunavuu:nunavut/1n}{mi:mi/tn-loc-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("nunavuumik", "{nunavum:nunavut/1n}{mik:mik/tn-acc-s}").isMisspelled());
		addCase(new AnalyzerCase("nunavuumit", "{nunavum:nunavut/1n}{mit:mit/tn-abl-s}").isMisspelled());
		addCase(new AnalyzerCase("nunavuumut", "{nunavum:nunavut/1n}{mut:mut/tn-dat-s}").isMisspelled());
		addCase(new AnalyzerCase("nunavuup", "{nunavu:nunavut/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("nunavuut", "{nunavu:nunavut/1n}{ut:up/tn-gen-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("nunnguani", "{nunngu:nunnguq/1n}{ani:ngani/tn-loc-s-4s}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("nuqqaqpuq", "{nuqqaq:nuqqaq/1v}{puq:vuq/tv-dec-3s}"));
		addCase(new AnalyzerCase("nutaami", "{nutaa:nutaaq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("nutaamik", "{nutaa:nutaaq/1n}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("nutaamut", "{nutaa:nutaaq/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("nutaanik", "{nutaa:nutaaq/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("nutaanit", "{nutaa:nutaaq/1n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("nutaanut", "{nutaa:nutaaq/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("nutaaq", "{nutaaq:nutaaq/1n}"));
		addCase(new AnalyzerCase("nutaat", "{nutaa:nutaaq/1n}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("nutarak", "[decomposition:/nutarak(nutarak)/]").isProperName());
		addCase(new AnalyzerCase("nutaraq", "[decomposition:/nutaraq(nutaraq)/]").isProperName());
		addCase(new AnalyzerCase("pa", "[decomposition:/pa(pa)/]").isProperName());
		addCase(new AnalyzerCase("paal", "[decomposition:/paal(paal)/]")
			.isProperName());
		addCase(new AnalyzerCase("paanapaasi", null)
			.isProperName()
			.comment("Barnabas"));
		addCase(new AnalyzerCase("paanapas", null)
			.isProperName()
			.comment("Barnabas"));
		addCase(new AnalyzerCase("paktaqtuqtut", "{paktak:paktak/1v}{tuq:tuq/1vv}{tut:jut/tv-ger-3p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("paliisikkut", "{paliisi:paliisi/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("panniqtuumi", "{panniqtuu:pangnirtuuq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("panniqtuuq", "{panniqtuuq:pangnirtuuq/1n}"));
		addCase(new AnalyzerCase("parnaut", "{parna:parnaq/1v}{ut:ut/1vn}"));
		addCase(new AnalyzerCase("parnautiit", "{parna:parnaq/1v}{uti:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("parnautinik", "{parna:parnaq/1v}{uti:ut/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("pattatuqtut", "{patta:paktak/1v}{tuq:tuq/1vv}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("paul", "[decomposition:/paul(paul)/]")
			.isProperName());
		addCase(new AnalyzerCase("paulusi", "[decomposition:/paulusi(paulusi)/]")
			.isProperName());
		addCase(new AnalyzerCase("pi", "[decomposition:/pi(pi)/]")
			.isProperName());
		addCase(new AnalyzerCase("pigiaqtitait", "{pi:pi/1v}{giaq:giaq/1vv}{ti:tit/1vv}{ta:jaq/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("pigiaqtitamut", "{pi:pi/1v}{giaq:giaq/1vv}{ti:tit/1vv}{ta:jaq/1vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("pigiaqtitaq", "{pi:pi/1v}{giaq:giaq/1vv}{ti:tit/1vv}{taq:jaq/1vn}"));
		addCase(new AnalyzerCase("pigiaqtitat", "{pi:pi/1v}{giaq:giaq/1vv}{ti:tit/1vv}{ta:jaq/1vn}{t:it/tn-nom-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("pigiaqtitaujuq", "{pi:pi/1v}{giaq:giaq/1vv}{ti:tit/1vv}{ta:jaq/1vn}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("pigiarutiksanit", "{pi:pi/1v}{gia:giaq/1vv}{ruti:ut/1vn}{ksa:ksaq/1nn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("piiku", null)
			.isProperName()
			.comment("Picco"));
		addCase(new AnalyzerCase("piikuu", null)
			.isProperName()
			.comment("Picco"));
		addCase(new AnalyzerCase("piita", null)
			.isProperName()
			.comment("Peter"));
		addCase(new AnalyzerCase("pijjutigillugit", "{pi:pi/1v}{jjuti:jjut/1vn}{gi:gi/1nv}{llugit:lugit/tv-part-1s-3p-prespas}"));
		addCase(new AnalyzerCase("pijjutigillugu", "{pi:pi/1v}{jjuti:jjut/1vn}{gi:gi/1nv}{llugu:lugu/tv-part-1s-3s-prespas}"));
		addCase(new AnalyzerCase("pijjutilik", "{pi:pi/1v}{jjuti:jjut/1vn}{lik:lik/1nn}"));
		addCase(new AnalyzerCase("pijjutiqaqtunik", "{pi:pi/1v}{jjuti:jjut/1vn}{qaq:qaq/1nv}{tu:juq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("pijjutiqaqtuq", "{pi:pi/1v}{jjuti:jjut/1vn}{qaq:qaq/1nv}{tuq:juq/1vn}"));
		addCase(new AnalyzerCase("pijjutiqaqtut", "{pi:pi/1v}{jjuti:jjut/1vn}{qaq:qaq/1nv}{tut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("piku", null)
			.isProperName()
			.comment("Picco"));
		addCase(new AnalyzerCase("piliriaksat", "{piliria:piliriaq/1n}{ksa:ksaq/1nn}{t:it/tn-nom-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("piliriamik", "{piliria:piliriaq/1n}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("piliriamut", "{piliria:piliriaq/1n}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("piliriangujuq", "{piliria:piliriaq/1n}{ngu:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("piliriangujut", "{piliria:piliriaq/1n}{ngu:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("pilirianik", "{piliria:piliriaq/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("pilirianut", "{piliria:piliriaq/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("piliriaq", "{piliriaq:piliriaq/1n}"));
		addCase(new AnalyzerCase("piliriat", "{piliria:piliriaq/1n}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("piliriatsaq", "{piliria:piliriaq/1n}{tsaq:ksaq/1nn}"));
		addCase(new AnalyzerCase("piliriviit", "{pi:pi/1n}{liri:liri/1nv}{vi:vik/3vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("pilirivik", "{pi:pi/1n}{liri:liri/1nv}{vik:vik/3vn}"));
		addCase(new AnalyzerCase("pilirivinga", "{pi:pi/1n}{liri:liri/1nv}{vi:vik/3vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("piliriviujunut", "{pi:pi/1n}{liri:liri/1nv}{vi:vik/3vn}{u:u/1nv}{ju:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("piliriviujuq", "{pi:pi/1n}{liri:liri/1nv}{vi:vik/3vn}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("piliriviujut", "{pi:pi/1n}{liri:liri/1nv}{vi:vik/3vn}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("piliriviup", "{pi:pi/1n}{liri:liri/1nv}{vi:vik/3vn}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("pilirivviit", "{pi:pi/1n}{liri:liri/1nv}{vvi:vik/3vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("pilirivvik", "{pi:pi/1n}{liri:liri/1nv}{vvik:vik/3vn}"));
		addCase(new AnalyzerCase("pilirivvinga", "{pi:pi/1n}{liri:liri/1nv}{vvi:vik/3vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("pillugit", "{pi:pi/1v}{llugit:lugit/tv-part-1s-3p-prespas}"));
		addCase(new AnalyzerCase("pillugu", "{pi:pi/1v}{llugu:lugu/tv-part-1s-3s-prespas}"));
		addCase(new AnalyzerCase("piluaqtumi", "{pi:pi/1v}{luaq:luaq/1vv}{tu:juq/1vn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("piluaqtumik", "{pi:pi/1v}{luaq:luaq/1vv}{tu:juq/1vn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("piluaqtumit", "{pi:pi/1v}{luaq:luaq/1vv}{tu:juq/1vn}{mit:mit/tn-abl-s}"));
		addCase(new AnalyzerCase("pinasuarusiulauqtumi", "{pinasuarusi:pinasuarusiq/1n}{u:u/1nv}{lauq:lauq/1vv}{tu:juq/1vn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("pinasuarusiup", "{pinasuarusi:pinasuarusiq/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("pingajuanni", "{pingajuan:pingajuat/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("pingajuannik", "{pingajuan:pingajuat/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("pingajuannit", "{pingajuan:pingajuat/1n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("pingajuat", "{pingajuat:pingajuat/1n}"));
		addCase(new AnalyzerCase("pingasunik", "{pingasu:pingasu/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("pingasunit", "{pingasu:pingasu/1n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("pingasunut", "{pingasu:pingasu/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("pingasut", "{pingasut:pingasut/1n}"));
		addCase(new AnalyzerCase("piqujaksait", "{pi:pi/1v}{qu:qu/2vv}{ja:jaq/1vn}{ksa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("piqujaksaq", "{pi:pi/1v}{qu:qu/2vv}{ja:jaq/1vn}{ksaq:ksaq/1nn}"));
		addCase(new AnalyzerCase("piqujaksat", "{pi:pi/1v}{qu:qu/2vv}{ja:jaq/1vn}{ksa:ksaq/1nn}{t:it/tn-nom-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("piqujaq", "{pi:pi/1v}{qu:qu/2vv}{jaq:jaq/1vn}"));
		addCase(new AnalyzerCase("pivalliatittinirmut", "{pi:pi/1v}{vallia:vallia/1vv}{tit:tit/1vv}{ti:si/1vv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("pu", "[decomposition:/pu(pu)/]")
			.isProperName());
		addCase(new AnalyzerCase("pukiqnaq", "[decomposition:/pukiqnaq(pukiqnaq)/]")
			.isProperName());
		addCase(new AnalyzerCase("pukirnak", "[decomposition:/pukirnak(pukirnak)/]")
			.isProperName());
		addCase(new AnalyzerCase("pukirnaq", "[decomposition:/pukirnaq(pukirnaq)/]")
			.isProperName());
		addCase(new AnalyzerCase("pukirngnak", "[decomposition:/pukirngnak(pukirngnak)/]")
			.isProperName());
		addCase(new AnalyzerCase("pukkirnaq", "[decomposition:/pukkirnaq(pukkirnaq)/]")
			.isProperName());
		addCase(new AnalyzerCase("pulaaqtinit", "{pulaaq:pulaaq/1v}{ti:ji/1vn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("pulaariaqsimajut", "{pulaa:pulaaq/1v}{riaq:giaq/1vv}{sima:sima/1vv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("pularaqtulirinirmut", "{pula:pula/1v}{raq:raq/1vv}{tu:juq/1vn}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("puqirnak", "[decomposition:/puqirnak(puqirnak)/]")
			.isProperName());
		addCase(new AnalyzerCase("puriimmia", "{puriimmia:puriimmia/1n}"));
		addCase(new AnalyzerCase("qallunaat", "{qallunaa:qaplunaaq/1n}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("qallunaatitut", "{qallunaaq:qaplunaaq/1n}{titut:titut/tn-sim-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("qamaniqtuaq", "{qamaniqtuaq:qamaniqjuaq/1n}"));
		addCase(new AnalyzerCase("qamaniqtuarmi", "{qamaniqtuar:qamaniqjuaq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("qamanittuarmi", "{qamanittuar:qamaniqjuaq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("qanga", "{qanga:qanga/1a}"));
		addCase(new AnalyzerCase("qangakkut", "{qanga:qanga/1a}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("qanu", "{qanu:qanuq/1a}"));
		addCase(new AnalyzerCase("qanuimmat", "{qanuim:qanuit/1v}{mat:mat/tv-caus-4s}"));
		addCase(new AnalyzerCase("qanuittuni", "{qanu:qanuq/1a}{it:it/3nv}{tu:juq/1vn}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("qanuittunik", "{qanu:qanuq/1a}{it:it/3nv}{tu:juq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("qanuq", "{qanuq:qanuq/1a}"));
		addCase(new AnalyzerCase("qanurli", "{qanur:qanuq/1a}{li:li/1q}"));
		addCase(new AnalyzerCase("qanutuinnaq", "{qanu:qanuq/1a}{tuinnaq:tuinnaq/2nn}"));
		addCase(new AnalyzerCase("qattinik", "{qatti:qapsit/1n}{nit:nit/tn-acc-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("qaujigiarutit", "{qauji:qauji/1v}{gia:giaq/1vv}{rut:ut/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("qaujigumajunga", "{qauji:qauji/1v}{guma:juma/1vv}{junga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("qaujijumajunga", "{qauji:qauji/1v}{juma:juma/1vv}{junga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("qaujijumatuinnaqpunga", "{qauji:qauji/1v}{juma:juma/1vv}{tuinnaq:tuinnaq/1vv}{punga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("qaujijumatuinnaqtunga", "{qauji:qauji/1v}{juma:juma/1vv}{tuinnaq:tuinnaq/1vv}{tunga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("qaujijumavunga", "{qauji:qauji/1v}{juma:juma/1vv}{vunga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("qaujimagama", "{qaujima:qaujima/1v}{gama:gama/tv-caus-1s}"));
		addCase(new AnalyzerCase("qaujimagatta", "{qaujima:qaujima/1v}{gatta:gatta/tv-caus-1p}"));
		addCase(new AnalyzerCase("qaujimagavit", "{qaujima:qaujima/1v}{gavit:gavit/tv-caus-2s}"));
		addCase(new AnalyzerCase("qaujimajatuqanginnik", "{qaujima:qaujima/1v}{ja:jaq/1vn}{tuqa:tuqaq/1nn}{nginnik:nginnik/tn-acc-p-4s}"));
		addCase(new AnalyzerCase("qaujimajatuqanginnit", "{qaujima:qaujima/1v}{ja:jaq/1vn}{tuqa:tuqaq/1nn}{nginnit:nginnit/tn-abl-p-4s}"));
		addCase(new AnalyzerCase("qaujimajatuqanginnut", "{qaujima:qaujima/1v}{ja:jaq/1vn}{tuqa:tuqaq/1nn}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("qaujimajatuqangit", "{qaujima:qaujima/1v}{ja:jaq/1vn}{tuqa:tuqaq/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("qaujimajuinnaugatta", "{qaujima:qaujima/1v}{ju:juq/1vn}{inna:innaq/1nn}{u:u/1nv}{gatta:gatta/tv-caus-1p}"));
		addCase(new AnalyzerCase("qaujimajunga", "{qaujima:qaujima/1v}{junga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("qaujimallunga", "{qaujima:qaujima/1v}{llunga:lunga/tv-part-1s-prespas}"));
		addCase(new AnalyzerCase("qaujimammata", "{qaujima:qaujima/1v}{mmata:mata/tv-caus-4p}"));
		addCase(new AnalyzerCase("qaujimanngilanga", "{qaujima:qaujima/1v}{nngi:nngit/1vv}{langa:langa/tv-dec-1s}"));
		addCase(new AnalyzerCase("qaujimanngittunga", "{qaujima:qaujima/1v}{nngit:nngit/1vv}{tunga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("qaujimavugut", "{qaujima:qaujima/1v}{vugut:vugut/tv-dec-1p}"));
		addCase(new AnalyzerCase("qaujimavunga", "{qaujima:qaujima/1v}{vunga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("qaujisarniq", "{qauji:qauji/1v}{sar:saq/1vv}{niq:niq/2vn}"));
		addCase(new AnalyzerCase("qaujisarnirmut", "{qauji:qauji/1v}{sar:saq/1vv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("qauppat", "{qauppat:qaukpat/1n}"));
		addCase(new AnalyzerCase("qautamaamut", "{qau:qau/1n}{tamaa:tamaaq/1nn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("qautamaat", "{qau:qau/1n}{tamaa:tamaaq/1nn}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("qikiqtaalummi", "{qikiqtaalum:qikiqtaaluk/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("qikiqtaalungmi", "{qikiqtaalung:qikiqtaaluk/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("qikiqtaaluup", "{qikiqtaalu:qikiqtaaluk/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("qilavva", "[decomposition:/qilavva(qilavva)/]")
			.isProperName());
		addCase(new AnalyzerCase("qilavvak", "[decomposition:/qilavvak(qilavvak)/]").isProperName());
		addCase(new AnalyzerCase("qimirrujiit", "{qimirru:qimirru/1v}{ji:ji/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("qimirruniq", "{qimirru:qimirru/1v}{niq:niq/2vn}"));
		addCase(new AnalyzerCase("qingaummi", "{qingaum:qingaq/2n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("qitingani", "{qiti:qitiq/1n}{ngani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("qitirmiuni", "{qitirmiu:qitirmiut/1n}{ni:ni/tn-loc-p}"));
		addCase(new AnalyzerCase("qitirmiunit", "{qitirmiu:qitirmiut/1n}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("qitirmiunut", "{qitirmiu:qitirmiut/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("qitirmiut", "{qitirmiut:qitirmiut/1n}"));
		addCase(new AnalyzerCase("qujannami", "{quja:quja/1v}{nna:naq/1vv}{mi:miik/1vn}"));
		addCase(new AnalyzerCase("qujannamii", "{qujannamii:qujannamiik/1a}"));
		addCase(new AnalyzerCase("qujannamiik", "{qujannamiik:qujannamiik/1a}"));
		addCase(new AnalyzerCase("qujannamiingujutit", "{qujannamii:qujannamiik/1a}{ngu:u/1nv}{jutit:jutit/tv-ger-2s}"));
		addCase(new AnalyzerCase("qujannamiirumajakka", "{qujannamii:qujannamiiq/1v}{ruma:juma/1vv}{jakka:jakka/tv-ger-1s-3p}"));
		addCase(new AnalyzerCase("qujannamiirumajara", "{qujannamii:qujannamiiq/1v}{ruma:juma/1vv}{jara:jara/tv-ger-1s-3s}"));
		addCase(new AnalyzerCase("qujannamiirumavakka", "{qujannamii:qujannamiiq/1v}{ruma:juma/1vv}{vakka:vakka/tv-dec-1s-3p}"));
		addCase(new AnalyzerCase("qujannamiirumavara", "{qujannamii:qujannamiiq/1v}{ruma:juma/1vv}{vara:vara/tv-dec-1s-3s}"));
		addCase(new AnalyzerCase("qujannamik", "{quja:quja/1v}{nna:naq/1vv}{mik:miik/1vn}")
			.isMisspelled());
		addCase(new AnalyzerCase("qulluktuq", "{qulluktuq:kugluktuk/1n}"));
		addCase(new AnalyzerCase("quppirniliit", "{quppir:quppiq/1v}{ni:niq/2vn}{li:lik/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("qurluqtuumi", "{qurluqtuu:kugluktuk/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("quviappunga", "{quviap:quviak/1v}{punga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("quviasukpunga", "{quvia:quviak/1v}{suk:suk/1vv}{punga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("quviattunga", "{quviat:quviak/1v}{tunga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("riipika", null)
			.isProperName()
			.comment("Rebecca"));
		addCase(new AnalyzerCase("saiman", null)
			.isProperName()
			.comment("Simon"));
		addCase(new AnalyzerCase("sanajulirijikkunnut", "{sana:sana/1v}{ju:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkun:kkut/1nn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("sanajulirijikkut", "{sana:sana/1v}{ju:juq/1vn}{liri:liri/1nv}{ji:ji/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("saniani", "{sani:sani/1n}{ani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("sanikiluaq", "{sanikiluaq:sanikiluaq/1n}"));
		addCase(new AnalyzerCase("sanikiluarmi", "{sanikiluar:sanikiluaq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("saqqitaujuq", "{saqqi:saqqik/1v}{ta:jaq/1vn}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("saqqitaujut", "{saqqi:saqqik/1v}{ta:jaq/1vn}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("saqqitauningit", "{saqqi:saqqik/1v}{ta:jaq/1vn}{u:u/1nv}{ni:niq/2vn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("saqqitausimajuq", "{saqqi:saqqik/1v}{ta:jaq/1vn}{u:u/1nv}{sima:sima/1vv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("silataani", "{silata:silata/1n}{ani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("silaup", "{sila:sila/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("sivuliqti", "{sivuliqti:sivuliqti/1n}"));
		addCase(new AnalyzerCase("sivuliqtii", "{sivuliqti:sivuliqti/1n}{i:k/tn-nom-d}"));
		addCase(new AnalyzerCase("sivuliqtiit", "{sivuliqti:sivuliqti/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("sivuliqtinginni", "{sivuliqti:sivuliqti/1n}{nginni:nginni/tn-loc-p-4s}"));
		addCase(new AnalyzerCase("sivuliqtiup", "{sivuliqti:sivuliqti/1n}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("sivuliuqti", "{sivuliuqti:sivuliuqti/1n}"));
		addCase(new AnalyzerCase("sivuliuqtii", "{sivuliuqti:sivuliuqti/1n}{i:k/tn-nom-d}"));
		addCase(new AnalyzerCase("sivulliqpaami", "{sivu:sivu/1n}{lliq:&iq/1nn}{paa:paaq/1nn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("sivulliqpaamik", "{sivu:sivu/1n}{lliq:&iq/1nn}{paa:paaq/1nn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("sivulliqpaamit", "{sivu:sivu/1n}{lliq:&iq/1nn}{paa:paaq/1nn}{mit:mit/tn-abl-s}"));
		addCase(new AnalyzerCase("sivulliqpaaq", "{sivu:sivu/1n}{lliq:&iq/1nn}{paaq:paaq/1nn}"));
		addCase(new AnalyzerCase("sivullirmi", "{sivu:sivu/1n}{llir:&iq/1nn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("sivullirmik", "{sivu:sivu/1n}{llir:&iq/1nn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("sivuniagut", "{sivuni:sivuni/1n}{agut:ngagut/tn-via-s-4s}"));
		addCase(new AnalyzerCase("sivuniani", "{sivuni:sivuni/1n}{ani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("sivuniksami", "{sivuni:sivuni/1n}{ksa:ksaq/1nn}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("sivuningani", "{sivuni:sivuni/1n}{ngani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("sivunittinni", "{sivuni:sivuni/1n}{ttinni:ttinni/tn-loc-s-1d}"));
		addCase(new AnalyzerCase("suli", "{suli:suli/1a}"));
		addCase(new AnalyzerCase("sulijuq", "{suli:suli/1a}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("sulikkanniiq", "{suli:suli/1a}{kkanniiq:kkanniq/1nn}")
			.isMisspelled());
		addCase(new AnalyzerCase("sulikkanniq", "{suli:suli/1a}{kkanniq:kkanniq/1nn}"));
		addCase(new AnalyzerCase("summat", "{su:su/1v}{mmat:mat/tv-caus-4s}"));
		addCase(new AnalyzerCase("surlu", "{surlu:suurlu/1a}")
			.isMisspelled());
		addCase(new AnalyzerCase("surusiit", "{surusi:surusiq/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("surusirnut", "{surusir:surusiq/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("suuqaimma", "{suuqaimma:suuqaimma/1e}"));
		addCase(new AnalyzerCase("suurlu", "{suurlu:suurlu/1a}"));
		addCase(new AnalyzerCase("suusan", "[decomposition:/suusan(suusan)/]")
			.isProperName());
		addCase(new AnalyzerCase("taakkua", "{taakkua:taakkua/pd-sc-p}"));
		addCase(new AnalyzerCase("taakkunani", "{taakku:taakku/rpd-sc-p}{nani:nani/tpd-loc-p}"));
		addCase(new AnalyzerCase("taakkunanngat", "{taakku:taakku/rpd-sc-p}{nanngat:nanngat/tpd-abl-p}"));
		addCase(new AnalyzerCase("taakkuninga", "{taakku:taakku/rpd-sc-p}{ninga:ninga/tpd-acc-p}"));
		addCase(new AnalyzerCase("taakkununga", "{taakku:taakku/rpd-sc-p}{nunga:nunga/tpd-dat-p}"));
		addCase(new AnalyzerCase("taaksumunga", "{taaksu:taapsu/rpd-sc-s}{munga:munga/tpd-dat-s}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("taalait", "{taala:taala/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("taampsan", "[decomposition:/taampsan(taampsan)/]")
			.isProperName());
		addCase(new AnalyzerCase("taamsan", "[decomposition:/taamsan(taamsan)/]")
			.isProperName());
		addCase(new AnalyzerCase("taanna", "{taanna:taamna/pd-sc-s}"));
		addCase(new AnalyzerCase("taanut", null)
			.isProperName()
			.comment("Donald"));
		addCase(new AnalyzerCase("taassuma", "{taassu:taapsu/rpd-sc-s}{ma:ma/tpd-gen-s}"));
		addCase(new AnalyzerCase("taassuminga", "{taassu:taapsu/rpd-sc-s}{minga:minga/tpd-acc-s}"));
		addCase(new AnalyzerCase("taassumunga", "{taassu:taapsu/rpd-sc-s}{munga:munga/tpd-dat-s}"));
		addCase(new AnalyzerCase("taatsuma", "{taatsu:taapsu/rpd-sc-s}{ma:ma/tpd-gen-s}"));
		addCase(new AnalyzerCase("taatsuminga", "{taatsu:taapsu/rpd-sc-s}{minga:minga/tpd-acc-s}"));
		addCase(new AnalyzerCase("tagvani", "{tagv:tagv/rad-sc}{ani:ani/tad-loc}"));
		addCase(new AnalyzerCase("taikani", "{taik:taik/rad-sc}{ani:ani/tad-loc}"));
		addCase(new AnalyzerCase("taikkua", "{taikkua:taikkua/pd-sc-p}"));
		addCase(new AnalyzerCase("taikkuninga", "{taikku:taikku/rpd-sc-p}{ninga:ninga/tpd-acc-p}"));
		addCase(new AnalyzerCase("taikkununga", "{taikku:taikku/rpd-sc-p}{nunga:nunga/tpd-dat-p}"));
		addCase(new AnalyzerCase("taiksumani", "{taiksu:taiksu/rpd-sc-s}{mani:mani/tpd-loc-s}"));
		addCase(new AnalyzerCase("taikua", "{taikua:taikkua/pd-sc-p}")
			.isMisspelled());
		addCase(new AnalyzerCase("taikunga", "{taik:taik/rad-sc}{unga:unga/tad-dat}"));
		addCase(new AnalyzerCase("taima", "{taima:taima/1a}"));
		addCase(new AnalyzerCase("taimaak", "{taimaak:taimaak/1a}"));
		addCase(new AnalyzerCase("taimaimmat", "{taimaim:taimait/1v}{mat:mat/tv-caus-4s}"));
		addCase(new AnalyzerCase("taimainninganut", "{taimain:taimait/1v}{ni:niq/2vn}{nganut:nganut/tn-dat-s-4s}"));
		addCase(new AnalyzerCase("taimaittumik", "{taimait:taimait/1v}{tu:juq/1vn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("taimaittunik", "{taimait:taimait/1v}{tu:juq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("taimak", "{taimak:taimaak/1a}")
			.isMisspelled());
		addCase(new AnalyzerCase("taimali", "{taima:taima/1a}{li:li/1q}"));
		addCase(new AnalyzerCase("taimaliqai", "{taima:taima/1a}{li:li/1q}{qai:qai/1q}"));
		addCase(new AnalyzerCase("taimanna", "{taimanna:taimanna/1a}"));
		addCase(new AnalyzerCase("taimannak", "{taimannak:taimanna/1a}")
			.isMisspelled());
		addCase(new AnalyzerCase("taimannaummat", "{taimanna:taimanna/1a}{u:u/1nv}{mmat:mat/tv-caus-4s}"));
		addCase(new AnalyzerCase("taimannganit", "{taimanngat:taimanngat/1a}{nit:nit/tn-abl-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("taimanngat", "{taimanngat:taimanngat/1a}"));
		addCase(new AnalyzerCase("tainna", "{tainna:taingna/pd-sc-s}"));
		addCase(new AnalyzerCase("taissumani", "{taissu:taiksu/rpd-sc-s}{mani:mani/tpd-loc-s}"));
		addCase(new AnalyzerCase("taitsumani", "{taitsu:taiksu/rpd-sc-s}{mani:mani/tpd-loc-s}"));
		addCase(new AnalyzerCase("taiviti", null)
			.isProperName()
			.comment("David"));
		addCase(new AnalyzerCase("takkua", "{takkua:taakkua/pd-sc-p}")
			.isMisspelled());
		addCase(new AnalyzerCase("tallimanut", "{tallima:tallima/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("tamaani", "{tama:tama/rad-ml}{ani:ani/tad-loc}"));
		addCase(new AnalyzerCase("tamajja", "{tamajja:tamajja/ad-ml}"));
		addCase(new AnalyzerCase("tamakkua", "{tamakkua:tamakkua/pd-ml-p}"));
		addCase(new AnalyzerCase("tamakkunani", "{tamakku:tamakku/rpd-ml-p}{nani:nani/tpd-loc-p}"));
		addCase(new AnalyzerCase("tamakkunanngat", "{tamakku:tamakku/rpd-ml-p}{nanngat:nanngat/tpd-abl-p}"));
		addCase(new AnalyzerCase("tamakkuninga", "{tamakku:tamakku/rpd-ml-p}{ninga:ninga/tpd-acc-p}"));
		addCase(new AnalyzerCase("tamakkuninnga", "{tamakku:tamakku/rpd-ml-p}{ninnga:ninga/tpd-acc-p}")
			.isMisspelled());
		addCase(new AnalyzerCase("tamakkununga", "{tamakku:tamakku/rpd-ml-p}{nunga:nunga/tpd-dat-p}"));
		addCase(new AnalyzerCase("tamaksuminga", "{tamaksu:tamaksu/rpd-ml-s}{minga:minga/tpd-acc-s}"));
		addCase(new AnalyzerCase("tamaksumunga", "{tamaksu:tamaksu/rpd-ml-s}{munga:munga/tpd-dat-s}"));
		addCase(new AnalyzerCase("tamani", "{tam:tama/rad-ml}{ani:ani/tad-loc}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("tamanna", "{tamanna:tamanna/pd-ml-s}"));
		addCase(new AnalyzerCase("tamannali", "{tamanna:tamanna/pd-ml-s}{li:li/1q}"));
		addCase(new AnalyzerCase("tamannalu", "{tamanna:tamanna/pd-ml-s}{lu:lu/1q}"));
		addCase(new AnalyzerCase("tamarmik", "{tamarmik:tamarmik/1p}"));
		addCase(new AnalyzerCase("tamassuminga", "{tamassu:tamaksu/rpd-ml-s}{minga:minga/tpd-acc-s}"));
		addCase(new AnalyzerCase("tamassumunga", "{tamassu:tamaksu/rpd-ml-s}{munga:munga/tpd-dat-s}"));
		addCase(new AnalyzerCase("tamatuma", "{tamatu:tamatu/rpd-ml-s}{ma:ma/tpd-gen-s}"));
		addCase(new AnalyzerCase("tamatumani", "{tamatu:tamatu/rpd-ml-s}{mani:mani/tpd-loc-s}"));
		addCase(new AnalyzerCase("tamatuminga", "{tamatu:tamatu/rpd-ml-s}{minga:minga/tpd-acc-s}"));
		addCase(new AnalyzerCase("tamatuminnga", "{tamatu:tamatu/rpd-ml-s}{minnga:minga/tpd-acc-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("tamatumunga", "{tamatu:tamatu/rpd-ml-s}{munga:munga/tpd-dat-s}"));
		addCase(new AnalyzerCase("tamatumunnga", "{tamatu:tamatu/rpd-ml-s}{munnga:munga/tpd-dat-s}")
			.isMisspelled());
		addCase(new AnalyzerCase("tamaunga", "{tama:tama/rad-ml}{unga:unga/tad-dat}"));
		addCase(new AnalyzerCase("tanna", "{tanna:taamna/pd-sc-s}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("taqqaani", "{taqqa:taqqa/rad-sc}{ani:ani/tad-loc}"));
		addCase(new AnalyzerCase("taqqakkununga", "{taqqakku:taqqapku/rpd-?-p}{nunga:nunga/tpd-dat-p}"));
		addCase(new AnalyzerCase("taqqiit", "{taqqi:taqqiq/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("tausan", "{tausan:tausat/1n}"));
		addCase(new AnalyzerCase("tavani", "{tav:tagv/rad-sc}{ani:ani/tad-loc}")
			.isMisspelled());
		addCase(new AnalyzerCase("tavva", "{tavva:tagva/ad-sc}"));
		addCase(new AnalyzerCase("tavvani", "{tavv:tagv/rad-sc}{ani:ani/tad-loc}"));
		addCase(new AnalyzerCase("tavvanngat", "{tavv:tagv/rad-sc}{anngat:anngat/tad-abl}"));
		addCase(new AnalyzerCase("tavvunga", "{tavv:tagv/rad-sc}{unga:unga/tad-dat}"));
		addCase(new AnalyzerCase("tavvuuna", "{tavv:tagv/rad-sc}{uuna:uuna/tad-via}"));
		addCase(new AnalyzerCase("tigusinirmut", "{tigu:tigu/1v}{si:si/2vv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("tikirarjuaq", "{tikirarjuaq:tikirarjuaq/1n}"));
		addCase(new AnalyzerCase("tikittugu", "{tikit:tikit/1v}{tugu:lugu/tv-part-1s-3s-prespas}"));
		addCase(new AnalyzerCase("timimigut", "{timi:timi/1n}{migut:migut/tn-via-s-3s}"));
		addCase(new AnalyzerCase("timiujuq", "{timi:timi/1n}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("timiujut", "{timi:timi/1n}{u:u/1nv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("titiqqak", null)
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; [decomposition:/titiqqak(titiqqaq)/]"));
		addCase(new AnalyzerCase("titiqqakkut", null)
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; [decomposition:/titiqqa(titiqqaq)/kkut(kkut)/]"));
		addCase(new AnalyzerCase("titiqqakkuvik", null)
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; [decomposition:/titiqqa(titiqqaq)/kku(kkut)/vik(vik)/]"));
		addCase(new AnalyzerCase("titiqqamik", null)
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; [decomposition:/titiqqa(titiqqaq)/mik(mik)/]"));
		addCase(new AnalyzerCase("titiqqanik", "[decomposition:/titiqqa(titiqqaq)/nik(nik)/]")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("titiqqanit", "[decomposition:/titiqqa(titiqqaq)/nit(nit)/]")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("titiqqaq", "[decomposition:/titiqqaq(titiqqaq)/]")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("titiqqat", "[decomposition:/titiqqat(titiqqat)/]")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("titiqqatigut", "[decomposition:/titiqqa(titiqqaq)/tigut(tigut)/]")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("titiraqsimajunik", "{titi:titiq/1v}{raq:raq/1vv}{sima:sima/1vv}{ju:juq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("titiraqsimajunut", "{titi:titiq/1v}{raq:raq/1vv}{sima:sima/1vv}{ju:juq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("titiraqsimajut", "{titi:titiq/1v}{raq:raq/1vv}{sima:sima/1vv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("titiraqsimaningit", "{titi:titiq/1v}{raq:raq/1vv}{sima:sima/1vv}{ni:niq/2vn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("titiraqti", "{titi:titiq/1v}{raq:raq/1vv}{ti:ji/1vn}"));
		addCase(new AnalyzerCase("titiraqtii", "{titi:titiq/1v}{raq:raq/1vv}{ti:ji/1vn}{i:it/tn-nom-p}"));
		addCase(new AnalyzerCase("tukiliuqtausimajuq", "{tuki:tuki/1n}{liuq:liuq/1nv}{ta:jaq/1vn}{u:u/1nv}{sima:sima/1vv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("tukisinaliqtissimajuq", "{tukisi:tukisi/1v}{na:naq/1vv}{liq:liq/1vv}{tis:tit/1vv}{sima:sima/1vv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("tuksiarniq", "{tuksiar:tuksiaq/1v}{niq:niq/2vn}"));
		addCase(new AnalyzerCase("tullia", "{tulli:tugli/1n}{a:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("tungaagut", "{tunga:tunga/1n}{agut:ngagut/tn-via-s-4s}"));
		addCase(new AnalyzerCase("tungilia", "{tungili:tungiliq/1n}{a:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("tungilinga", "{tungili:tungiliq/1n}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("tungilira", "{tungili:tungiliq/1n}{ra:ga/tn-nom-s-1s}"));
		addCase(new AnalyzerCase("tunngasugit", "{tunnga:tunnga/1v}{su:suk/1vv}{git:git/tv-imp-2s}"));
		addCase(new AnalyzerCase("tunngasugitsi", "{tunnga:tunnga/1v}{su:suk/1vv}{gitsi:gipsi/tv-imp-2p}"));
		addCase(new AnalyzerCase("tunngavikkut", "{tunnga:tunnga/1v}{vi:vik/3vn}{kkut:kkut/1nn}")
			.comment("Tunngavik Nunavut Inc."));
		addCase(new AnalyzerCase("tununiq", "{tununiq:tununiq/1n}"));
		addCase(new AnalyzerCase("tupiq", "{tupiq:tupiq/1n}"));
		addCase(new AnalyzerCase("turaangajunik", "{turaa:turaaq/1v}{nga:nga/1vv}{ju:juq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("turaangajunit", "{turaa:turaaq/1v}{nga:nga/1vv}{ju:juq/1vn}{nit:nit/tn-abl-p}"));
		addCase(new AnalyzerCase("turaangajuq", "{turaa:turaaq/1v}{nga:nga/1vv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("turaangajut", "{turaa:turaaq/1v}{nga:nga/1vv}{jut:jut/tv-ger-3p}"));
		addCase(new AnalyzerCase("tusaajaqtuqsimajunik", "{tusaa:tusaa/1v}{jaqtuq:jaqtuq/1vv}{sima:sima/1vv}{ju:juq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("tusaaji", "{tusaaji:tusaaji/1n}"));
		addCase(new AnalyzerCase("tusaajikkut", "{tusaaji:tusaaji/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("tusaajitigu", "{tusaaji:tusaaji/1n}{tigu:tigut/tn-via-p}"));
		addCase(new AnalyzerCase("tusaajitigut", "{tusaaji:tusaaji/1n}{tigut:tigut/tn-via-p}"));
		addCase(new AnalyzerCase("tusaajitiguuqtuq", "{tusaaji:tusaaji/1n}{tigu:tigut/tn-via-p}{uq:uq/1nv}{tuq:juq/1vn}"));
		addCase(new AnalyzerCase("tusaajitiguurunniiqtuq", "{tusaaji:tusaaji/1n}{tigu:tigut/tn-via-p}{u:uq/1nv}{runniiq:junniiq/1vv}{tuq:juq/1vn}"));
		addCase(new AnalyzerCase("tusaajititigu", "{tusaaji:tusaaji/1n}{titigut:tigut/tn-via-p}")
			.isMisspelled());
		addCase(new AnalyzerCase("tusarumatuinnaqtunga", "{tusa:tusaq/1v}{ruma:juma/1vv}{tuinnaq:tuinnaq/1vv}{tunga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("tutu", "[decomposition:/tutu(tutu)/]")
			.isProperName());
		addCase(new AnalyzerCase("tutuu", "[decomposition:/tutuu(tutuu)/]")
			.isProperName());
		addCase(new AnalyzerCase("tuutu", "[decomposition:/tuutu(tuutu)/]")
			.isProperName());
		addCase(new AnalyzerCase("tuutuu", "[decomposition:/tuutuu(tuutuu)/]")
			.isProperName());
		addCase(new AnalyzerCase("puraian", null)
			.isProperName()
			.comment("Brian"));
		addCase(new AnalyzerCase("vuraian", null)
			.isProperName()
			.comment("Brian"));
		addCase(new AnalyzerCase("vuraijan", null)
			.isProperName()
			.comment("Brian"));
		addCase(new AnalyzerCase("uannangani", "{uanna:uangnaq/1n}{ngani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("uattiaq", "{uattiaq:uattiaq/1a}"));
		addCase(new AnalyzerCase("uattiaru", "{uattiaru:uattiaruk/1a}"));
		addCase(new AnalyzerCase("uqaalautaa", "{uqa:uqaq/1v}{ala:allak/1vv}{uta:ut/1vn}{a:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("uiliams", "[decomposition:/uiliams(uiliams)/]")
			.isProperName());
		addCase(new AnalyzerCase("ukaliq", "[decomposition:/ukaliq(ukaliq)/]")
			.isProperName());
		addCase(new AnalyzerCase("ukiukkut", "{ukiu:ukiuq/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("ukiunik", "{ukiu:ukiuq/1n}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("ukiunut", "{ukiu:ukiuq/1n}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("ukiuqtaqtumi", "{ukiuqtaqtu:ukiuqtaqtu/1n}{mi:mi/tn-loc-s}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("ukiurtartumi", "{ukiurtartu:ukiuqtaqtu/1n}{mi:mi/tn-loc-s}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("ukua", "{ukua:ukua/pd-sc-p}"));
		addCase(new AnalyzerCase("ukunani", "{uku:uku/1v}{nani:nani/tv-part-3s}"));
		addCase(new AnalyzerCase("ukuninga", "{uku:uku/rpd-sc-p}{ninga:ninga/tpd-acc-p}"));
		addCase(new AnalyzerCase("ukununga", "{uku:uku/rpd-sc-p}{nunga:nunga/tpd-dat-p}"));
		addCase(new AnalyzerCase("ulaaju", "[decomposition:/ulaaju(ulaaju)/]")
			.isProperName());
		addCase(new AnalyzerCase("ulaajuk", "[decomposition:/ulaajuk(ulaajuk)/]")
			.isProperName());
		addCase(new AnalyzerCase("ulaajuq", "[decomposition:/ulaajuq(ulaajuq)/]")
			.isProperName());
		addCase(new AnalyzerCase("ullaakkut", "{ullaa:ublaaq/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("ullaaq", "{ullaaq:ublaaq/1n}"));
		addCase(new AnalyzerCase("ulluit", "{ullu:ubluq/1n}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("ullumi", "{ullumi:ullumi/1a}"));
		addCase(new AnalyzerCase("ullumimut", "{ullumi:ullumi/1a}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("ullumiujuq", "{ullumi:ullumi/1a}{u:u/1nv}{juq:juq/1vn}"));
		addCase(new AnalyzerCase("ulluq", "{ulluq:ubluq/1n}"));
		addCase(new AnalyzerCase("una", "{una:una/pd-sc-s}"));
		addCase(new AnalyzerCase("ungataani", "{ungata:ungata/1n}{ani:ngani/tn-loc-s-4s}"));
		addCase(new AnalyzerCase("ungataanut", "{ungata:ungata/1n}{anut:nganut/tn-dat-s-4s}"));
		addCase(new AnalyzerCase("unikkaangat", "{unikkaa:unipkaaq/1n}{ngat:ngat/tn-nom-s-4p}"));
		addCase(new AnalyzerCase("unikkaangit", "{unikkaa:unipkaaq/1n}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("unikkaaq", "{unikkaaq:unipkaaq/1n}"));
		addCase(new AnalyzerCase("unikkaat", "{unikkaa:unipkaaq/1n}{t:it/tn-nom-p}"));
		addCase(new AnalyzerCase("unnuk", "{unnuk:unnuk/1n}"));
		addCase(new AnalyzerCase("unnukkut", "{unnu:unnuk/1n}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("unnusakkut", "{unnusa:unnuksaq/1n}{kkut:kkut/tn-via-s}"));
		addCase(new AnalyzerCase("unnusami", "{unnusa:unnuksaq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("upinnarani", "{upinnarani:upinnarani/1a}"));
		addCase(new AnalyzerCase("uqalaurama", "{uqa:uqaq/1v}{lau:lauq/1vv}{rama:gama/tv-caus-1s}"));
		addCase(new AnalyzerCase("uqalaurmat", "{uqa:uqaq/1v}{laur:lauq/1vv}{mat:mat/tv-caus-4s}"));
		addCase(new AnalyzerCase("uqalimaaqtauninga", "{uqa:uqaq/1v}{limaaq:limaaq/2vv}{ta:jaq/1vn}{u:u/1nv}{ni:niq/2vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("uqalimaaqtauninginnut", "{uqa:uqaq/1v}{limaaq:limaaq/2vv}{ta:jaq/1vn}{u:u/1nv}{ni:niq/2vn}{nginnut:nginnut/tn-dat-p-4s}"));
		addCase(new AnalyzerCase("uqalimaaqtauningit", "{uqa:uqaq/1v}{limaaq:limaaq/2vv}{ta:jaq/1vn}{u:u/1nv}{ni:niq/2vn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("uqalimaaqtauqullugu", "{uqa:uqaq/1v}{limaaq:limaaq/2vv}{ta:jaq/1vn}{u:u/1nv}{qu:qu/2vv}{llugu:lugu/tv-part-1s-3s-prespas}"));
		addCase(new AnalyzerCase("uqalimaarniq", "{uqa:uqaq/1v}{limaar:limaaq/2vv}{niq:niq/2vn}"));
		addCase(new AnalyzerCase("uqalimaarvik", "{uqa:uqaq/1v}{limaar:limaaq/2vv}{vik:vik/3vn}"));
		addCase(new AnalyzerCase("uqaqqaugama", "{uqa:uqaq/1v}{qqau:qqau/1vv}{gama:gama/tv-caus-1s}"));
		addCase(new AnalyzerCase("uqaqqaugamailaak", "{uqa:uqaq/1v}{qqau:qqau/1vv}{gama:gama/tv-caus-1s}{ilaak:ilaak/1q}"));
		addCase(new AnalyzerCase("uqaqqaugavit", "{uqa:uqaq/1v}{qqau:qqau/1vv}{gavit:gavit/tv-caus-2s}"));
		addCase(new AnalyzerCase("uqaqqaummat", "{uqa:uqaq/1v}{qqau:qqau/1vv}{mmat:mat/tv-caus-4s}"));
		addCase(new AnalyzerCase("uqaqsimammat", "{uqaq:uqaq/1v}{sima:sima/1vv}{mmat:mat/tv-caus-4s}"));
		addCase(new AnalyzerCase("uqaqti", "{uqaq:uqaq/1v}{ti:ji/1vn}"));
		addCase(new AnalyzerCase("uqaqtii", "{uqaq:uqaq/1v}{ti:ji/1vn}{i:k/tn-nom-d}"));
		addCase(new AnalyzerCase("uqaqtiup", "{uqaq:uqaq/1v}{ti:ji/1vn}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("uqartii", "{uqar:uqaq/1v}{ti:ji/1vn}{i:it/tn-nom-p}"));
		addCase(new AnalyzerCase("uqarumajunga", "{uqa:uqaq/1v}{ruma:juma/1vv}{junga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("uqarumavunga", "{uqa:uqaq/1v}{ruma:juma/1vv}{vunga:vunga/tv-dec-1s}"));
		addCase(new AnalyzerCase("uqarunnaqtunga", "{uqa:uqaq/1v}{runnaq:junnaq/1vv}{tunga:junga/tv-ger-1s}"));
		addCase(new AnalyzerCase("uqausiit", "{uqa:uqaq/1v}{usi:usiq/1vn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("uqausikkut", "{uqa:uqaq/1v}{usi:usiq/1vn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("uqausiksait", "{uqa:uqaq/1v}{usi:usiq/1vn}{ksa:ksaq/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("uqausiksangit", "{uqa:uqaq/1v}{usi:usiq/1vn}{ksa:ksaq/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("uqausiksat", "{uqausi:uqausiq/1n}{ksa:ksaq/1nn}{t:it/tn-nom-p}")
				.possiblyMisspelledWord()
				.comment("TODO-BF: Please add a SHORT comment; "));
		addCase(new AnalyzerCase("uqausilirinirmut", "{uqa:uqaq/1v}{usi:usiq/1vn}{liri:liri/1nv}{nir:niq/2vn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("uqausinga", "{uqa:uqaq/1v}{usi:usiq/1vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("uqausingit", "{uqa:uqaq/1v}{usi:usiq/1vn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("uqausiq", "{uqa:uqaq/1v}{usiq:usiq/1vn}"));
		addCase(new AnalyzerCase("uqausiqtigut", "{uqa:uqaq/1v}{usiq:usiq/1vn}{tigut:tigut/tn-via-p}"));
		addCase(new AnalyzerCase("uqausirijanga", "{uqa:uqaq/1v}{usi:usiq/1vn}{ri:gi/1nv}{ja:jaq/1vn}{nga:nga/tn-nom-s-4s}"));
		addCase(new AnalyzerCase("uqausirmik", "{uqa:uqaq/1v}{usir:usiq/1vn}{mik:mik/tn-acc-s}"));
		addCase(new AnalyzerCase("uqausirnik", "{uqa:uqaq/1v}{usir:usiq/1vn}{nik:nik/tn-acc-p}"));
		addCase(new AnalyzerCase("uqausirnut", "{uqa:uqaq/1v}{usir:usiq/1vn}{nut:nut/tn-dat-p}"));
		addCase(new AnalyzerCase("uqausissangit", "{uqa:uqaq/1v}{usi:usiq/1vn}{ssa:ksaq/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("uqausitsangit", "{uqa:uqaq/1v}{usi:usiq/1vn}{tsa:ksaq/1nn}{ngit:ngit/tn-nom-p-4s}"));
		addCase(new AnalyzerCase("uqausituinnakkut", "{uqa:uqaq/1v}{usi:usiq/1vn}{tuinna:tuinnaq/2nn}{kkut:kkut/1nn}"));
		addCase(new AnalyzerCase("uqqurmiut", "{uqqurmiut:uqqurmiut/1n}"));
		addCase(new AnalyzerCase("uqsualuit", "{uqsu:uqsuq/1n}{alu:aluk/1nn}{it:it/tn-nom-p}"));
		addCase(new AnalyzerCase("uqsualuk", "{uqsu:uqsuq/1n}{aluk:aluk/1nn}"));
		addCase(new AnalyzerCase("uqsualummut", "{uqsu:uqsuq/1n}{alum:aluk/1nn}{mut:mut/tn-dat-s}"));
		addCase(new AnalyzerCase("uqsualuup", "{uqsu:uqsuq/1n}{alu:aluk/1nn}{up:up/tn-gen-s}"));
		addCase(new AnalyzerCase("uqsuqtuumi", "{uqsuqtuu:uqsuqtuuq/1n}{mi:mi/tn-loc-s}"));
		addCase(new AnalyzerCase("utirluta", "{utir:utiq/1v}{luta:luta/tv-part-1p-fut}"));
		addCase(new AnalyzerCase("utupiri", "{utupiri:utupiri/1n}"));
		addCase(new AnalyzerCase("uuktutigilugu", "{uuk:uuk/1v}{tu:tuq/1vv}{ti:ut/1vn}{gi:gi/1nv}{lugu:lugu/tv-part-1s-3s-fut}")
			.isMisspelled());
		addCase(new AnalyzerCase("uuktuutigilugu", "{uuk:uuk/1v}{tu:tuq/1vv}{uti:ut/1vn}{gi:gi/1nv}{lugu:lugu/tv-part-1s-3s-fut}"));
		addCase(new AnalyzerCase("uupraijan", null)
			.isProperName()
			.comment("O'brien"));
		addCase(new AnalyzerCase("uuttuutigillugu", "{uut:uuk/1v}{tu:tuq/1vv}{uti:ut/1vn}{gi:gi/1nv}{llugu:lugu/tv-part-1s-3s-prespas}"));
		addCase(new AnalyzerCase("uuttuutigilugu", "{uut:uuk/1v}{tu:tuq/1vv}{uti:ut/1vn}{gi:gi/1nv}{lugu:lugu/tv-part-1s-3s-fut}"));
		addCase(new AnalyzerCase("uuviti", null)
			.isProperName()
			.comment("Ovide"));
		addCase(new AnalyzerCase("uuvuraian", null)
			.isProperName()
			.comment("O'brien"));
		addCase(new AnalyzerCase("uuvuraijan", null)
			.isProperName()
			.comment("O'brien"));
		addCase(new AnalyzerCase("uvagut", "{uvagut:uvagut/1p}"));
		addCase(new AnalyzerCase("uvalu", "{uvalu:uvvalu/1c}")
			.isMisspelled());
		addCase(new AnalyzerCase("uvanga", "{uvanga:uvanga/1p}"));
		addCase(new AnalyzerCase("uvani", "{uv:uv/rad-sc}{ani:ani/tad-loc}"));
		addCase(new AnalyzerCase("uvannik", "{uva:uva/1rpr}{nnik:nnik/tn-acc-s-1s}"));
		addCase(new AnalyzerCase("uvannut", "{uva:uva/1rpr}{nnut:nnut/tn-dat-s-1s}"));
		addCase(new AnalyzerCase("uvattinni", "{uva:uva/1rpr}{ttinni:ttinni/tn-loc-p-1p}"));
		addCase(new AnalyzerCase("uvattinnik", "{uva:uva/1rpr}{ttinnik:ttinnik/tn-acc-p-1p}"));
		addCase(new AnalyzerCase("uvattinnut", "{uva:uva/1rpr}{ttinnut:ttinnut/tn-dat-p-1p}"));
		addCase(new AnalyzerCase("uvunga", "{uv:uv/rad-sc}{unga:unga/tad-dat}"));
		addCase(new AnalyzerCase("uvva", "{uvva:ubva/ad-sc}"));
		addCase(new AnalyzerCase("uvvalu", "{uvvalu:uvvalu/1c}"));
		addCase(new AnalyzerCase("uvvalukiaq", "{uvvalu:uvvalu/1c}{kiaq:kia/1q}"));
		addCase(new AnalyzerCase("uvvaluunni", "{uvvaluunni:uvvaluunniit/1c}")
			.isMisspelled());
		addCase(new AnalyzerCase("uvvaluunniit", "{uvvaluunniit:uvvaluunniit/1c}"));
		addCase(new AnalyzerCase("vaanavas", null)
			.isProperName()
			.comment("Barnabas"));
		addCase(new AnalyzerCase("viliams", "[decomposition:/viliams(viliams)/]")
			.isProperName());
		addCase(new AnalyzerCase("viuris", null)
			.isProperName()
			.comment("Ferris"));
		addCase(new AnalyzerCase("vivvuari", "{vivvuari:vivvuari/1n}"));		
	}
}
