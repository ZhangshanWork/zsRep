package im.vinci.server.naturelang.domain;

import java.io.Serializable;

public class XMLYSemantic implements Serializable {
    private String catalog;
    private String subCatalog;
    private String album;
    private String name;
    private String albumId;

    public XMLYSemantic() {
    }

    public String getCatalog() {
        return this.catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSubCatalog() {
        return this.subCatalog;
    }

    public void setSubCatalog(String subCatalog) {
        this.subCatalog = subCatalog;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbumId() {
        return this.albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }
}
