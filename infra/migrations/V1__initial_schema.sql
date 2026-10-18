
BEGIN;

CREATE EXTENSION IF NOT EXISTS postgis;


-- 1. USERS
--so this will store the basic user information like name, email, phone, password and so on... of the member and provider

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash TEXT,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 2. COMMUNITIES
--and this will store the community information like name, description, address, location and so on...

CREATE TABLE communities (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    address TEXT,
    location GEOGRAPHY(POINT, 4326),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 3. COMMUNITY MEMBERSHIPS
--basically this will allow the member to be a provider aswell, and vice versa so that they can receive and provide services

CREATE TABLE community_memberships (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    community_id BIGINT NOT NULL REFERENCES communities(id),
    role VARCHAR(20) NOT NULL
        CHECK (role IN ('MEMBER', 'PROVIDER')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, community_id, role)
);


-- 4. SERVICE CATEGORIES
-- user and member can choose the service they want or provide the service they want to give by selecting from drop down options.

CREATE TABLE service_categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT REFERENCES service_categories(id),
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);


-- 5. PROVIDER PROFILES
--this shows whetheer the provider is available or not and if they accepted the gig or not

CREATE TABLE provider_profiles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    description TEXT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (
            verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')
        ),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 6. SERVICES OFFERED BY PROVIDERS
--one provider can offer multiple services or can have multiple roles and they can maintain all this in a single profile

CREATE TABLE provider_services (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES provider_profiles(id),
    community_id BIGINT NOT NULL REFERENCES communities(id),
    service_category_id BIGINT NOT NULL
        REFERENCES service_categories(id),
    price_from NUMERIC(12,2) CHECK (price_from >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (provider_id, community_id, service_category_id)
);


-- 7. MEMBER REQUIREMENTS
-- this shows the requirements of the member like what they want to buy, rent or get a service from the provider

CREATE TABLE requirements (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    community_id BIGINT NOT NULL REFERENCES communities(id),
    created_by BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL
        CHECK (type IN ('SERVICE', 'PRODUCT', 'RENTAL')),
    service_category_id BIGINT REFERENCES service_categories(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    quantity NUMERIC(12,2) CHECK (quantity > 0),
    quantity_unit VARCHAR(40),
    budget NUMERIC(12,2) CHECK (budget >= 0),
    needed_by TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (
            status IN ('OPEN', 'MATCHED', 'FULFILLED', 'CANCELLED')
        ),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (
        (type = 'SERVICE' AND service_category_id IS NOT NULL)
        OR
        (type IN ('PRODUCT', 'RENTAL') AND service_category_id IS NULL)
    )
);


-- 8. SERVICE GIGS
--shows how the provider will be offering the service, whether it is hourly, fixed or quote based and etc etc

CREATE TABLE gigs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider_service_id BIGINT NOT NULL
        REFERENCES provider_services(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    pricing_type VARCHAR(20) NOT NULL DEFAULT 'FIXED'
        CHECK (pricing_type IN ('FIXED', 'HOURLY', 'QUOTE')),
    price NUMERIC(12,2) CHECK (price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'PAUSED', 'CLOSED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 9. PRODUCT CATEGORIES AND LISTINGS

CREATE TABLE product_categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT REFERENCES product_categories(id),
    name VARCHAR(120) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE product_listings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES provider_profiles(id),
    community_id BIGINT NOT NULL REFERENCES communities(id),
    product_category_id BIGINT REFERENCES product_categories(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    unit VARCHAR(40) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    available_quantity NUMERIC(12,2) NOT NULL DEFAULT 0
        CHECK (available_quantity >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 10. RENTAL LISTINGS

CREATE TABLE rental_listings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES provider_profiles(id),
    community_id BIGINT NOT NULL REFERENCES communities(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price_per_day NUMERIC(12,2) NOT NULL
        CHECK (price_per_day >= 0),
    deposit_amount NUMERIC(12,2) NOT NULL DEFAULT 0
        CHECK (deposit_amount >= 0),
    available_quantity INTEGER NOT NULL DEFAULT 1
        CHECK (available_quantity >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);


-- 11. GROUP BUYING CAMPAIGNS

CREATE TABLE group_buy_campaigns (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    community_id BIGINT NOT NULL REFERENCES communities(id),
    product_listing_id BIGINT NOT NULL REFERENCES product_listings(id),
    created_by BIGINT NOT NULL REFERENCES users(id),
    target_quantity NUMERIC(12,2) NOT NULL CHECK (target_quantity > 0),
    current_quantity NUMERIC(12,2) NOT NULL DEFAULT 0
        CHECK (current_quantity >= 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    starts_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ends_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (
            status IN ('OPEN', 'SUCCESS', 'FAILED', 'CANCELLED')
        ),
    CHECK (ends_at > starts_at)
);

CREATE TABLE group_buy_participants (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES group_buy_campaigns(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    quantity NUMERIC(12,2) NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'JOINED'
        CHECK (status IN ('JOINED', 'WITHDRAWN', 'CONFIRMED')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (campaign_id, user_id)
);


-- 12. SERVICE BOOKINGS

CREATE TABLE bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    gig_id BIGINT NOT NULL REFERENCES gigs(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    scheduled_at TIMESTAMPTZ,
    agreed_amount NUMERIC(12,2) NOT NULL CHECK (agreed_amount >= 0),
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED'
        CHECK (
            status IN (
                'REQUESTED', 'ACCEPTED', 'IN_PROGRESS',
                'PROVIDER_COMPLETED', 'CUSTOMER_CONFIRMED',
                'CLOSED', 'CANCELLED', 'DISPUTED'
            )
        ),
    provider_completed_at TIMESTAMPTZ,
    customer_confirmed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 13. ORDERS AND ORDER ITEMS

CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    community_id BIGINT NOT NULL REFERENCES communities(id),
    campaign_id BIGINT REFERENCES group_buy_campaigns(id),
    status VARCHAR(25) NOT NULL DEFAULT 'PENDING'
        CHECK (
            status IN (
                'PENDING', 'CONFIRMED', 'PROCESSING',
                'READY', 'DELIVERED', 'CANCELLED', 'DISPUTED'
            )
        ),
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    delivery_fee NUMERIC(12,2) NOT NULL DEFAULT 0
        CHECK (delivery_fee >= 0),
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_listing_id BIGINT REFERENCES product_listings(id),
    rental_listing_id BIGINT REFERENCES rental_listings(id),
    item_name VARCHAR(200) NOT NULL,
    quantity NUMERIC(12,2) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    line_total NUMERIC(12,2) NOT NULL CHECK (line_total >= 0),
    rental_start TIMESTAMPTZ,
    rental_end TIMESTAMPTZ,
    CHECK (
        num_nonnulls(product_listing_id, rental_listing_id) = 1
    ),
    CHECK (
        (rental_start IS NULL AND rental_end IS NULL)
        OR
        (rental_start IS NOT NULL AND rental_end > rental_start)
    )
);


-- 14. PAYMENTS

CREATE TABLE payments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    booking_id BIGINT REFERENCES bookings(id),
    gateway VARCHAR(60) NOT NULL,
    gateway_payment_id VARCHAR(200),
    idempotency_key VARCHAR(200) NOT NULL UNIQUE,
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(25) NOT NULL DEFAULT 'CREATED'
        CHECK (
            status IN (
                'CREATED', 'PENDING', 'AUTHORIZED', 'SUCCEEDED',
                'FAILED', 'CANCELLED', 'PARTIALLY_REFUNDED', 'REFUNDED'
            )
        ),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (num_nonnulls(order_id, booking_id) = 1),
    UNIQUE (gateway, gateway_payment_id)
);

CREATE TABLE payment_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    event_type VARCHAR(80) NOT NULL,
    gateway_event_id VARCHAR(200) UNIQUE,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE refunds (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    gateway_refund_id VARCHAR(200) UNIQUE,
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED'
        CHECK (
            status IN ('REQUESTED', 'PENDING', 'SUCCEEDED', 'FAILED')
        ),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payouts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES provider_profiles(id),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    gateway_payout_id VARCHAR(200) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (
            status IN ('PENDING', 'PROCESSING', 'PAID', 'FAILED', 'CANCELLED')
        ),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 15. COMPLAINTS

CREATE TABLE complaints (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    order_id BIGINT REFERENCES orders(id),
    raised_by BIGINT NOT NULL REFERENCES users(id),
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (
            status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'REJECTED')
        ),
    resolution TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (num_nonnulls(booking_id, order_id) = 1)
);


-- 16. BOOKING AND ORDER EVENT HISTORY

CREATE TABLE task_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    order_id BIGINT REFERENCES orders(id),
    actor_id BIGINT REFERENCES users(id),
    event_type VARCHAR(80) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (num_nonnulls(booking_id, order_id) = 1)
);


-- 17. PROVIDER RELIABILITY METRICS

CREATE TABLE provider_metrics (
    provider_id BIGINT PRIMARY KEY REFERENCES provider_profiles(id),
    completed_bookings INTEGER NOT NULL DEFAULT 0
        CHECK (completed_bookings >= 0),
    resolved_complaints INTEGER NOT NULL DEFAULT 0
        CHECK (resolved_complaints >= 0),
    last_calculated_at TIMESTAMPTZ
);


-- 18. ATTACHMENTS
-- Actual files live in object storage.

CREATE TABLE attachments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uploaded_by BIGINT NOT NULL REFERENCES users(id),
    storage_key TEXT NOT NULL UNIQUE,
    file_name TEXT NOT NULL,
    content_type VARCHAR(150),
    size_bytes BIGINT CHECK (size_bytes >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 19. NOTIFICATIONS

CREATE TABLE notifications (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(80) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    data JSONB NOT NULL DEFAULT '{}'::jsonb,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- 20. INDEXES

CREATE INDEX idx_communities_location
    ON communities USING GIST(location);

CREATE INDEX idx_memberships_community
    ON community_memberships(community_id, status);

CREATE INDEX idx_provider_services_category
    ON provider_services(service_category_id, community_id);

CREATE INDEX idx_requirements_feed
    ON requirements(community_id, status, created_at DESC);

CREATE INDEX idx_bookings_customer
    ON bookings(customer_id, created_at DESC);

CREATE INDEX idx_gigs_provider_service
    ON gigs(provider_service_id, status);

CREATE INDEX idx_orders_buyer
    ON orders(buyer_id, created_at DESC);

CREATE INDEX idx_payments_order
    ON payments(order_id);

CREATE INDEX idx_payments_booking
    ON payments(booking_id);

CREATE INDEX idx_notifications_user
    ON notifications(user_id, created_at DESC);

COMMIT;
