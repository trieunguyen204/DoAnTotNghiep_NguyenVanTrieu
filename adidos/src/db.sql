
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20),
    avatar_url TEXT,
    role VARCHAR(50),
    status VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_provider (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    provider VARCHAR(50) NOT NULL,        -- LOCAL, GOOGLE, FACEBOOK
    provider_id VARCHAR(255),             -- ID từ provider (Google/Facebook)
    email VARCHAR(255),                  -- email từ provider (có thể null)
    access_token TEXT,                   -- optional

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key
    CONSTRAINT fk_user_provider_user 
        FOREIGN KEY (user_id)
        REFERENCES user(id)
        ON DELETE CASCADE,

    -- Unique tránh trùng tài khoản từ provider
    CONSTRAINT uk_provider UNIQUE (provider, provider_id)
);

CREATE TABLE address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(255),
    phone VARCHAR(20),
    province VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    address_detail TEXT,
    is_default BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_address_user FOREIGN KEY (user_id)
        REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT,

    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id)
        REFERENCES category(id) ON DELETE SET NULL
);

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    brand VARCHAR(100),
    material VARCHAR(100),
    gender VARCHAR(20),
    category_id BIGINT,
    status VARCHAR(50),

    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
        REFERENCES category(id)
);

CREATE TABLE color (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    color_name VARCHAR(100) NOT NULL
);

CREATE TABLE sizes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    size_name VARCHAR(50) NOT NULL
);


CREATE TABLE product_variant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    stock_quantity INT DEFAULT 0,
	size_id BIGINT NOT NULL,
    color_id BIGINT NOT NULL,
    price DECIMAL(15, 2) NOT NULL DEFAULT 0.00,

    CONSTRAINT fk_variant_product FOREIGN KEY (product_id)
        REFERENCES product(id) ON DELETE CASCADE,

    CONSTRAINT fk_variant_size FOREIGN KEY (size_id)
        REFERENCES sizes(id),

    CONSTRAINT fk_variant_color FOREIGN KEY (color_id)
        REFERENCES color(id),

    CONSTRAINT uk_variant UNIQUE (product_id, size_id, color_id)
);


CREATE TABLE product_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_variant_id BIGINT NOT NULL,
    image_url TEXT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,

    CONSTRAINT fk_image_variant FOREIGN KEY (product_variant_id)
        REFERENCES product_variant(id) ON DELETE CASCADE
);


CREATE TABLE cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT fk_cart_user FOREIGN KEY (user_id)
        REFERENCES user(id) ON DELETE CASCADE,

    CONSTRAINT fk_cart_variant FOREIGN KEY (product_variant_id)
        REFERENCES product_variant(id) ON DELETE CASCADE,

    CONSTRAINT uk_cart UNIQUE (user_id, product_variant_id)
);

CREATE TABLE order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(255),
    shipping_address TEXT,
    total_price DECIMAL(12,2),
    shipping_fee DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    order_status VARCHAR(50),
    payment_status VARCHAR(50),
    voucher_id BIGINT,

    CONSTRAINT fk_order_user FOREIGN KEY (user_id)
        REFERENCES user(id)
);


CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    price DECIMAL(10,2),
    quantity INT,
    color VARCHAR(100),
    size VARCHAR(50),

    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
        REFERENCES `order`(id) ON DELETE CASCADE,

    CONSTRAINT fk_order_item_variant FOREIGN KEY (product_variant_id)
        REFERENCES product_variant(id)
);


CREATE TABLE payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50),
    transaction_code VARCHAR(255),
    amount DECIMAL(12,2),
    status VARCHAR(50),

    CONSTRAINT fk_payment_order FOREIGN KEY (order_id)
        REFERENCES `order`(id) ON DELETE CASCADE
);

CREATE TABLE voucher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) UNIQUE,
    discount_type VARCHAR(20),
    discount_value DECIMAL(10,2),
    min_order_value DECIMAL(10,2),
    quantity INT,
    start_date DATETIME,
    end_date DATETIME,
    status VARCHAR(50)
);

CREATE TABLE promotion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    promotion_name VARCHAR(255),
    discount_type VARCHAR(20),
    discount_value DECIMAL(10,2),
    max_discount_value DECIMAL(10,2),
    priority INT DEFAULT 0,
    start_date DATETIME,
    end_date DATETIME,
    status VARCHAR(50)
);

CREATE TABLE promotion_category (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
    promotion_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,

    CONSTRAINT fk_pc_promotion FOREIGN KEY (promotion_id)
        REFERENCES promotion(id) ON DELETE CASCADE,

    CONSTRAINT fk_pc_category FOREIGN KEY (category_id)
        REFERENCES category(id) ON DELETE CASCADE
);

CREATE TABLE review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_variant_id BIGINT,
    order_item_id BIGINT,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    parent_id BIGINT,

    CONSTRAINT fk_review_user FOREIGN KEY (user_id)
        REFERENCES user(id) ON DELETE CASCADE,

    CONSTRAINT fk_review_product FOREIGN KEY (product_id)
        REFERENCES product(id),

    CONSTRAINT fk_review_variant FOREIGN KEY (product_variant_id)
        REFERENCES product_variant(id),

    CONSTRAINT fk_review_order_item FOREIGN KEY (order_item_id)
        REFERENCES order_item(id),

    CONSTRAINT fk_review_parent FOREIGN KEY (parent_id)
        REFERENCES review(id)
);