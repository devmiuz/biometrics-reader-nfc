package corn.cardreader.model;

import android.os.Bundle;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import corn.cardreader.cadastre.CadastreListItem;

public class BundleKey {

    public static final String LICENCE_NUM_KEY = "license_num";
    public static final String DATE_OF_BIRTH_KEY = "birth_date";
    public static final String DATE_OF_EXP_KEY = "exp_date";

    public static final String LANGUAGE = "lang";

    public static final String BUNDLE = "bundle";

    public static final String CADASTRE_DG1_FILE = "cadastre_dg1";

    public static final String CADASTRE_LIST_ITEMS = "cadastre_list_items";
    public static final String CADASTRE_ID = "cadastreID";

//    public static CadastreListItem getCadastre(Bundle bundle){
//        String itemAsString = bundle.getString(BundleKey.CADASTRE_DG1_FILE);
//        Gson gson = new GsonBuilder().create();
//        CadastreListItem item = gson.fromJson(itemAsString, CadastreListItem.class);
//        return item;
//    }
//
//    public static Bundle setCadastre(CadastreListItem item){
//        Bundle bundle = new Bundle();
//        Gson gson = new GsonBuilder().create();
//        String cadastreAsString  = gson.toJson(item);
//        bundle.putString(BundleKey.CADASTRE_DG1_FILE, cadastreAsString);
//        return bundle;
//    }

}
