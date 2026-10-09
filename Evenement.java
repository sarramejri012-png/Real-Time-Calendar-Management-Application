/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agenda.model;
import java.time.LocalDate;
/**
 *
 * @author DELL
 */
public class Evenement {
    
    private int id;
    private String titre;
    private LocalDate date;
    private String heure;
    private String description;
    private String typeEvt;
    private boolean secret;

    public Evenement() {}

    public Evenement(int id, String titre, LocalDate date,
                     String heure, String description, String typeEvt, boolean secret) {
        this.id          = id;
        this.titre       = titre;
        this.date        = date;
        this.heure       = heure;
        this.description = description;
        this.typeEvt     = typeEvt;
        this.secret      = secret;
    }

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getTitre()              { return titre; }
    public void setTitre(String t)        { this.titre = t; }

    public LocalDate getDate()            { return date; }
    public void setDate(LocalDate d)      { this.date = d; }

    public String getHeure()              { return heure; }
    public void setHeure(String h)        { this.heure = h; }

    public String getDescription()        { return description; }
    public void setDescription(String d)  { this.description = d; }

    public String getTypeEvt()            { return typeEvt; }
    public void setTypeEvt(String t)      { this.typeEvt = t; }

    public boolean isSecret()             { return secret; }
    public void setSecret(boolean s)      { this.secret = s; }
}
