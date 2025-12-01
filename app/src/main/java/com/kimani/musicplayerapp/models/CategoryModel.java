package com.kimani.musicplayerapp.models;

public class CategoryModel {
    private String name;
    private String coverUrl;

    // A no-argument constructor is required for Firebase to work.
    public CategoryModel() {
    }

    public CategoryModel(String name, String coverUrl) {
        this.name = name;
        this.coverUrl = coverUrl;
    }

    // Getter for 'name'
    public String getName() {
        return name;
    }

    // Setter for 'name'
    public void setName(String name) {
        this.name = name;
    }

    // Getter for 'coverUrl'
    public String getCoverUrl() {
        return coverUrl;
    }

    // Setter for 'coverUrl'
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
