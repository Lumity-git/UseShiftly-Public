-- Initial schema for hotel scheduler
-- This file creates all tables, including employees

CREATE TABLE public.employees (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255) NOT NULL,
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    phone_number character varying(255),
    role character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    department_id bigint,
    building_id bigint,
    uuid character varying(255),
    CONSTRAINT employees_role_check CHECK (((role)::text = ANY ((ARRAY['EMPLOYEE'::character varying, 'MANAGER'::character varying, 'ADMIN'::character varying])::text[])))
);

ALTER TABLE public.employees OWNER TO scheduler_user;

CREATE SEQUENCE public.employees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.employees_id_seq OWNER TO scheduler_user;
ALTER SEQUENCE public.employees_id_seq OWNED BY public.employees.id;
