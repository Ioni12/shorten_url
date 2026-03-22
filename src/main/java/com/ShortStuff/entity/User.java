package com.ShortStuff.entity;

import jakarta.persistence.*;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false)
    private String provider;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getProvider() { return provider; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setProvider(String provider) { this.provider = provider; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String email;
        private String password;
        private String provider;

        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }

        public User build() {
            User user = new User();
            user.email = this.email;
            user.password = this.password;
            user.provider = this.provider;
            return user;
        }
    }
}
