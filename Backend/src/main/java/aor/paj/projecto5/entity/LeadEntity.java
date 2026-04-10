package aor.paj.projecto5.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import aor.paj.projecto5.utils.LeadState;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")

//alterado para só mostrar as leads que não estão soft deleted
@NamedQuery(name = "lead.findLeadsByUserId", query = "SELECT l FROM LeadEntity l WHERE l.owner.id = :id and l.softDeleted=false" )
@NamedQuery(name = "lead.findLeadsByUserIdLeadId", query = "SELECT l FROM LeadEntity l WHERE l.owner.id = :resourceId and l.id=:leadId")
@NamedQuery(name= "lead.findLeadByLeadID", query = "select l from LeadEntity l where l.id=:leadId")
@NamedQuery(name="lead.findSoftDelUserLeads", query="select l from LeadEntity l where l.softDeleted=true AND l.owner.id = :id")
@NamedQuery(name="lead.findLeadByState", query = "select l from LeadEntity l where l.leadState=:leadState")

public class LeadEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,updatable = false,unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column (name="state", nullable = false)
    private LeadState leadState=LeadState.NOVO; //todos começam neste estado

    @Column(name="titulo", nullable = false, updatable = true, unique = false)
    private String titulo;

    @Column(name="descricao", nullable = false,updatable = true, unique = false, length = 65535, columnDefinition = "TEXT")
    private String descricao;

    //@todo isto vai requerer alteração no frontend
    @CreationTimestamp
    @Column(name="data", nullable = false, updatable = false, unique = false)
    private LocalDateTime data;

    @Column(name = "softDeleted", nullable = false)
    private boolean softDeleted = false;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserEntity owner;


    //construtor vazio, obrigatório
    public LeadEntity(){}

    public LeadState getLeadState() {
        return leadState;
    }

    public void setLeadState(LeadState leadState) {
        this.leadState = leadState;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public boolean isSoftDeleted() {
        return softDeleted;
    }

    public void setSoftDeleted(boolean softDeleted) {
        this.softDeleted = softDeleted;
    }

    public Long getId() {
        return id;
    }
}
