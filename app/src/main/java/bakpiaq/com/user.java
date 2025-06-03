package bakpiaq.com;

public class user {
    private int urutan;
    private String id_akun;
    private String username;
    private String email;
    private int jabatan;
    private String jabatan_nama;
    private String dibuat_pada;
    private String redirect_url;

    public  user(){}
    public user(int urutan, String id_akun, String username, String email, int jabatan, String jabatan_nama, String dibuat_pada, String redirect_url) {
        this.urutan = urutan;
        this.id_akun = id_akun;
        this.username = username;
        this.email = email;
        this.jabatan = jabatan;
        this.jabatan_nama = jabatan_nama;
        this.dibuat_pada = dibuat_pada;
        this.redirect_url = redirect_url;
    }

    public int getUrutan() {
        return urutan;
    }

    public void setUrutan(int urutan) {
        this.urutan = urutan;
    }

    public String getId_akun() {
        return id_akun;
    }

    public void setId_akun(String id_akun) {
        this.id_akun = id_akun;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getJabatan() {
        return jabatan;
    }

    public void setJabatan(int jabatan) {
        this.jabatan = jabatan;
    }

    public String getJabatan_nama() {
        return jabatan_nama;
    }

    public void setJabatan_nama(String jabatan_nama) {
        this.jabatan_nama = jabatan_nama;
    }

    public String getDibuat_pada() {
        return dibuat_pada;
    }

    public void setDibuat_pada(String dibuat_pada) {
        this.dibuat_pada = dibuat_pada;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }
}
