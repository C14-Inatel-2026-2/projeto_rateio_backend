package rateio.membro.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name= "membro")
public class Membro {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String hashSenha;

    public Membro(){}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHashSenha() {return hashSenha;}
    public void setHashSenha(String hashSenha) {this.hashSenha = hashSenha;}
}
