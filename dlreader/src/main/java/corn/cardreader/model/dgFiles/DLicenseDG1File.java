package corn.cardreader.model.dgFiles;

import androidx.annotation.Nullable;
import corn.cardreader.model.LicenseCategory;

public class DLicenseDG1File {

    private String firstName;
    private String lastName;
    private String middleName;
    private String dateOfBirth;
    private String licenceNumber;
    private String issueState;
    private String dateOfExpiration;
    private String dateOfIssue;
    private String countryName;

    private LicenseCategory[] categories;

    public DLicenseDG1File(String firstName, String lastName, @Nullable String middleName,
                           String dateOfBirth, String countryName, String licenceNumber,
                           String issueState, String dateOfExpiration, String dateOfIssue) {
        setFirstName(firstName);
        setLastName(lastName);
        setMiddleName(middleName);
        this.dateOfBirth = getFormattedDateString(dateOfBirth);
        this.countryName = countryName;
        this.licenceNumber = licenceNumber;
        this.issueState = issueState;
        this.dateOfExpiration = getFormattedDateString(dateOfExpiration);
        this.dateOfIssue = getFormattedDateString(dateOfIssue);
    }

    public void setFirstName(String firstName) {
        this.firstName = getFormattedNameString(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = getFormattedNameString(lastName);
    }

    public void setMiddleName(String middleName) {
        this.middleName = getFormattedNameString(middleName);
    }

    public String getFormattedNameString(String name){
        if(name == null || name.isEmpty())
            return name;

        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public String getIssueState() {
        return issueState;
    }

    public String getDateOfExpiration() {
        return dateOfExpiration;
    }

    public String getDateOfIssue() {
        return dateOfIssue;
    }

    public String getCountryName() {
        return countryName;
    }

    public LicenseCategory[] getCategories() {
        return categories;
    }

    public void setCategories(LicenseCategory[] categories) {
        this.categories = categories;
    }

    public String getFormattedDateString(String date){
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6);
        return String.format("%s.%s.%s", day, month, year);
    }
    
    public String getFullName(){
        if(middleName == null || middleName.isEmpty())
            return String.format("%s %s", firstName, lastName);

        return String.format("%s %s %s", firstName, middleName, lastName);
    }

    public String getCategoriesAsString(){
        String res = "";
        for(LicenseCategory c : categories){
            if(c != null)
                res += c.getName() + " ";
        }

        return res;
    }
}
