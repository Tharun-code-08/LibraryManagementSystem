package com.university.lms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** Presentation-safe projection of {@code Book}. */
public final class BookDTO {

    private final Long id;
    private final String isbn;
    private final String title;
    private final String subtitle;
    private final String edition;
    private final String volume;
    private final String language;
    private final String publisherName;
    private final String categoryName;
    private final Set<String> authorNames;
    private final Set<String> tagNames;
    private final BigDecimal cost;
    private final LocalDate purchaseDate;
    private final String vendor;
    private final String coverImagePath;
    private final String qrCodePath;
    private final boolean deleted;
    private final int totalCopies;
    private final int availableCopies;

    private BookDTO(Builder builder) {
        this.id = builder.id;
        this.isbn = builder.isbn;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.edition = builder.edition;
        this.volume = builder.volume;
        this.language = builder.language;
        this.publisherName = builder.publisherName;
        this.categoryName = builder.categoryName;
        this.authorNames = builder.authorNames;
        this.tagNames = builder.tagNames;
        this.cost = builder.cost;
        this.purchaseDate = builder.purchaseDate;
        this.vendor = builder.vendor;
        this.coverImagePath = builder.coverImagePath;
        this.qrCodePath = builder.qrCodePath;
        this.deleted = builder.deleted;
        this.totalCopies = builder.totalCopies;
        this.availableCopies = builder.availableCopies;
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

    public String getPublisherName() {
        return publisherName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Set<String> getAuthorNames() {
        return authorNames;
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

    public String getQrCodePath() {
        return qrCodePath;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
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
        private String publisherName;
        private String categoryName;
        private Set<String> authorNames;
        private Set<String> tagNames;
        private BigDecimal cost;
        private LocalDate purchaseDate;
        private String vendor;
        private String coverImagePath;
        private String qrCodePath;
        private boolean deleted;
        private int totalCopies;
        private int availableCopies;

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

        public Builder publisherName(String publisherName) {
            this.publisherName = publisherName;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder authorNames(Set<String> authorNames) {
            this.authorNames = authorNames;
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

        public Builder qrCodePath(String qrCodePath) {
            this.qrCodePath = qrCodePath;
            return this;
        }

        public Builder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder totalCopies(int totalCopies) {
            this.totalCopies = totalCopies;
            return this;
        }

        public Builder availableCopies(int availableCopies) {
            this.availableCopies = availableCopies;
            return this;
        }

        public BookDTO build() {
            return new BookDTO(this);
        }
    }
}
