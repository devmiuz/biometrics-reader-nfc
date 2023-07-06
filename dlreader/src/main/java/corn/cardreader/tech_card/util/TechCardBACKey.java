package corn.cardreader.tech_card.util;

public class TechCardBACKey {

    private String regNumber;
    private String issueDate;
    private String licenseNum;

    public TechCardBACKey(String regNumber, String issueDate, String licenseNum) {
        this.regNumber = regNumber;
        this.issueDate = issueDate;
        this.licenseNum = licenseNum;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getLicenseNum() {
        return licenseNum;
    }

    public void setLicenseNum(String licenseNum) {
        this.licenseNum = licenseNum;
    }
}
