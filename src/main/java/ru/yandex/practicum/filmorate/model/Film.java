package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    @JsonProperty("mpa")
    private MPA mpa;
    @JsonIgnore
    private Set<Integer> likes = new HashSet<>();
    private TreeSet<Genre> genres = new TreeSet<>((o1, o2) -> Integer.compare(o1.getId(), o2.getId()));

    public void addLikes(int id) {
        likes.add(id);
    }

    public void deleteLikes(int id) {
        likes.remove(id);
    }

    public int getQuantityLikes() {
        return likes.size();
    }
}