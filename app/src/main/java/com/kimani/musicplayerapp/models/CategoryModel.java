package com.kimani.musicplayerapp.models;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CategoryModel represents a grouping of songs, such as an album or a genre.
 * This class is designed to be compatible with Firebase Firestore for automated data mapping.
 */
public class CategoryModel {

    private String name;           // The display name of the category (e.g., "Afrobeat")
    private String coverUrl;       // URL pointing to the category's cover image
    private List<String> songs;    // List of song IDs belonging to this category

    /**
     * Default no-argument constructor.
     * Required by Firebase Firestore for deserializing documents into Java objects.
     */
    public CategoryModel() {
        this.name = "";
        this.coverUrl = "";
        this.songs = Collections.emptyList();
    }

    /**
     * Parameterized constructor for manual instantiation.
     * @param name Name of the category.
     * @param coverUrl URL for the category image.
     * @param songs List of IDs for songs in this category.
     */
    public CategoryModel(String name, String coverUrl, List<String> songs) {
        this.name = name;
        this.coverUrl = coverUrl;
        this.songs = songs;
    }

    // --- GETTER METHODS ---

    public String getName() {
        return name;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * @return A list of Firestore document IDs corresponding to the songs in this category.
     */
    public List<String> getSongs() {
        return songs;
    }

    // --- SETTER METHODS ---

    public void setName(String name) {
        this.name = name;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setSongs(List<String> songs) {
        this.songs = songs;
    }

    /**
     * Compares this category with another object for equality based on its fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryModel that = (CategoryModel) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(coverUrl, that.coverUrl) &&
                Objects.equals(songs, that.songs);
    }

    /**
     * Generates a hash code for the category based on its fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, coverUrl, songs);
    }

    /**
     * Returns a string representation of the CategoryModel for debugging purposes.
     */
    @Override
    public String toString() {
        return "CategoryModel{" +
                "name='" + name + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", songs=" + songs +
                '}';
    }
}
