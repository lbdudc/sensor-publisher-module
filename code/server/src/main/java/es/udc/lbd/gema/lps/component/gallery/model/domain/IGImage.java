/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
package es.udc.lbd.gema.lps.component.gallery.model.domain;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "ig_image")
public class IGImage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imageGen")
    @SequenceGenerator(name = "imageGen", sequenceName = "ig_image_id_seq", allocationSize = 1)
    private Long id;

    private String title;

    private String path;

    @Column(columnDefinition="TEXT")
    private String description;

    private String author;

    private Integer year;

    private Instant creationDate;

    @OneToOne
    private IGGallery galeria;

    private Integer version;

    public IGImage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public IGGallery getGaleria() {
        return galeria;
    }

    public void setGaleria(IGGallery galeria) {
        this.galeria = galeria;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
/*% } %*/
