//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.validation;

import cs.utils.FilesUtil;
import java.util.HashMap;
import java.util.List;

public class Analysis {
    public Analysis() {
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, String> psMap = new HashMap();
        String psAll = "/Users/kashifrabbani/Documents/GitHub/qse/validation/example/test/city_ps_all.csv";
        List<String[]> psAllData = FilesUtil.readCsvAllDataOnceWithCustomSeparator(psAll, ',');
        psAllData.forEach((val) -> {
            psMap.put(val[0], null);
        });
        String psSuppConfA = "/Users/kashifrabbani/Documents/GitHub/qse/validation/example/test/city_ps_supconf_a.csv";
        List<String[]> psSuppConfAData = FilesUtil.readCsvAllDataOnceWithCustomSeparator(psSuppConfA, ',');
        psSuppConfAData.forEach((val) -> {
            if (psMap.containsKey(val[0])) {
                psMap.put(val[0], val[1] + "," + val[2]);
            } else {
                System.out.println("A -> " + val[0]);
            }

        });
        String psSuppConfB = "/Users/kashifrabbani/Documents/GitHub/qse/validation/example/test/city_ps_supconf_b.csv";
        List<String[]> psSuppConfBData = FilesUtil.readCsvAllDataOnceWithCustomSeparator(psSuppConfB, ',');
        psSuppConfBData.forEach((val) -> {
            if (psMap.containsKey(val[0])) {
                psMap.put(val[0], val[1] + "," + val[2]);
            } else {
                System.out.println("B -> " + val[0]);
            }

        });
        System.out.println("HASHMAP");
        psMap.forEach((k, v) -> {
            if (v == null) {
                System.out.println(k + " -> " + v);
            }

        });
    }
}
