package corn.cardreader.model.dgFiles;

public class DLicenseDG2File {

    private String birthPlaceRegion;
    private String _pinfl;
    private String residenceAddress;
    private String residenceRegion;
    private String residenceDistrict;

    public DLicenseDG2File(String birthPlaceRegion, String _pinfl, String residenceAddress, String residenceRegion, String residenceDistrict) {
        this.birthPlaceRegion = birthPlaceRegion;
        this._pinfl = _pinfl;
        this.residenceAddress = residenceAddress;
        this.residenceRegion = residenceRegion;
        this.residenceDistrict = residenceDistrict;
    }

    public String getBirthPlace(){
        return birthPlaceRegion;
    }

    public String getResidenceAddress(){
        if(residenceDistrict != null || !residenceDistrict.isEmpty()){
            String result = residenceRegion;
            if (result != null && !result.isEmpty()) result += ",";
            result += residenceDistrict;
            if (result.isEmpty() || result.endsWith(",")) result += residenceAddress;
            else result += ", " + residenceAddress;
            if (result.startsWith(",")) return result.substring(1);
            return result.trim();
        }

        if(residenceRegion != null || !residenceRegion.isEmpty()){
            return String.format("%s, %s", residenceRegion, residenceAddress);
        }

        return residenceAddress;
    }
}
