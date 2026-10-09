/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agenda.dao;
import agenda.db.DBConnection;
import agenda.model.Evenement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author DELL
 */
public class EvenementDAO {
     public void ajouter(Evenement e) throws SQLException {
        String sql = "INSERT INTO EVENEMENT (TITRE, DATE_EVT, HEURE, DESCRIPTION, TYPE_EVT, SECRET) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setDate(2, Date.valueOf(e.getDate()));
            ps.setString(3, e.getHeure());
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getTypeEvt());
            ps.setInt(6, e.isSecret() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void modifier(Evenement e) throws SQLException {
        String sql = "UPDATE EVENEMENT SET TITRE=?, DATE_EVT=?, HEURE=?, " +
                     "DESCRIPTION=?, TYPE_EVT=?, SECRET=? WHERE ID=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setDate(2, Date.valueOf(e.getDate()));
            ps.setString(3, e.getHeure());
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getTypeEvt());
            ps.setInt(6, e.isSecret() ? 1 : 0);
            ps.setInt(7, e.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM EVENEMENT WHERE ID = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Tous les evenements normaux (non secrets)
    public List<Evenement> listerTous() throws SQLException {
        List<Evenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM EVENEMENT WHERE SECRET = 0 ORDER BY DATE_EVT, HEURE";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    // Evenements secrets uniquement
    public List<Evenement> listerSecrets() throws SQLException {
        List<Evenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM EVENEMENT WHERE SECRET = 1 ORDER BY DATE_EVT, HEURE";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    // Recherche avec filtre — si secretOnly = true, demande code avant
    public List<Evenement> rechercher(String titre, LocalDate date,
                                       String type, boolean secretOnly) throws SQLException {
        List<Evenement> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM EVENEMENT WHERE SECRET = ");
        sql.append(secretOnly ? "1" : "0");

        if (titre != null && !titre.isBlank())
            sql.append(" AND UPPER(TITRE) LIKE UPPER(?)");
        if (date != null)
            sql.append(" AND DATE_EVT = ?");
        if (type != null && !type.isBlank() && !type.equals("Tous"))
            sql.append(" AND TYPE_EVT = ?");

        sql.append(" ORDER BY DATE_EVT, HEURE");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (titre != null && !titre.isBlank())
                ps.setString(idx++, "%" + titre + "%");
            if (date != null)
                ps.setDate(idx++, Date.valueOf(date));
            if (type != null && !type.isBlank() && !type.equals("Tous"))
                ps.setString(idx, type);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    private Evenement mapRow(ResultSet rs) throws SQLException {
        Evenement e = new Evenement(
            rs.getInt("ID"),
            rs.getString("TITRE"),
            rs.getDate("DATE_EVT").toLocalDate(),
            rs.getString("HEURE"),
            rs.getString("DESCRIPTION"),
            rs.getString("TYPE_EVT"),
            rs.getInt("SECRET") == 1
        );
        return e;
    }
}
