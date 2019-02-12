package tony.dev.mohamed.myuber.models;

public class UberDriver {
    private String name;
    private String phone;
    private String email;
    private String photo;
    private String rates;
    private String vechleType;
    public UberDriver() {
    }
    public UberDriver(String name, String phone, String email, String photo, String rates, String vechleType) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.photo = photo;
        this.rates = rates;
        this.vechleType = vechleType;
    }


    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getVechleType() {
        return vechleType;
    }

    public void setVechleType(String vechleType) {
        this.vechleType = vechleType;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
