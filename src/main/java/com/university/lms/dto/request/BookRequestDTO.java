package com.university.lms.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** Input to {@code BookService.createBook}/{@code updateBook} — {@code id == null} means create. */
public final class BookRequestDTO {

    private final Long id;
    private final String isbn;
    private final String title;
    private final String subtitle;
    private final String edition;
    private final String volume;
    private final String language;
    private final Long publisherId;
    private final Long categoryId;
    private final Set<Long> authorIds;
    private final Set<String> tagNames;
    private final BigDecimal cost;
    private final LocalDate purchaseDate;
    private final String vendor;
    private final String coverImagePath;

    private BookRequestDTO(Builder builder) {
        this.id = builder.id;
        this.isbn = builder.isbn;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.edition = builder.edition;
        this.volume = builder.volume;
        this.language = builder.language;
        this.publisherId = builder.publisherId;
        this.categoryId = builder.categoryId;
        this.authorIds = builder.authorIds;
        this.tagNames = builder.tagNames;
        this.cost = builder.cost;
        this.purchaseDate = builder.purchaseDate;
        this.vendor = builder.vendor;
        this.coverImagePath = builder.coverImagePath;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getEdition() {
        return edition;
    }

    public String getVolume() {
        return volume;
    }

    public String getLanguage() {
        return language;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Set<Long> getAuthorIds() {
        return authorIds;
    }

    public Set<String> getTagNames() {
        return tagNames;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public String getVendor() {
        return vendor;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String isbn;
        private String title;
        private String subtitle;
        private String edition;
        private String volume;
        private String language;
        private Long publisherId;
        private Long categoryId;
        private Set<Long> authorIds;
        private Set<String> tagNames;
        private BigDecimal cost;
        private LocalDate purchaseDate;
        private String vendor;
        private String coverImagePath;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder edition(String edition) {
            this.edition = edition;
            return this;
        }

        public Builder volume(String volume) {
            this.volume = volume;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder publisherId(Long publisherId) {
            this.publisherId = publisherId;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder authorIds(Set<Long> authorIds) {
            this.authorIds = authorIds;
            return this;
        }

        public Builder tagNames(Set<String> tagNames) {
            this.tagNames = tagNames;
            return this;
        }

        public Builder cost(BigDecimal cost) {
            this.cost = cost;
            return this;
        }

        public Builder purchaseDate(LocalDate purchaseDate) {
            this.purchaseDate = purchaseDate;
            return this;
        }

        public Builder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder coverImagePath(String coverImagePath) {
            this.coverImagePath = coverImagePath;
            return this;
        }

        public BookRequestDTO build() {
            return new BookRequestDTO(this);
        }
    }
}
