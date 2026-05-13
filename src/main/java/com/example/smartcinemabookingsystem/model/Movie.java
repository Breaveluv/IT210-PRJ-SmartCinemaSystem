package com.example.smartcinemabookingsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Tên phim không được để trống")
    @Size(max = 255, message = "Tên phim không được vượt quá 255 ký tự")
    private String title;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Mô tả phim không được để trống")
    private String description;

    @Min(value = 1, message = "Thời lượng phim phải lớn hơn 0 phút")
    private int duration;

    @NotBlank(message = "URL poster không được để trống")
    private String posterUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "movie_genres",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @NotNull(message = "Phim phải có ít nhất một thể loại")
    private Set<Genre> genres = new HashSet<>();
}
