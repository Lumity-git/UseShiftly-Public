--
-- PostgreSQL database dump
--

-- Dumped from database version 16.9 (Ubuntu 16.9-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.9 (Ubuntu 16.9-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: shift_trade_status_enum; Type: TYPE; Schema: public; Owner: scheduler_user
--

CREATE TYPE public.shift_trade_status_enum AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED',
    'POSTED_TO_EVERYONE',
    'PENDING_APPROVAL'
);


ALTER TYPE public.shift_trade_status_enum OWNER TO scheduler_user;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: building; Type: TABLE; Schema: public; Owner: scheduler_user
--

CREATE TABLE public.building (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.building OWNER TO scheduler_user;

--
-- Name: building_id_seq; Type: SEQUENCE; Schema: public; Owner: scheduler_user
--

CREATE SEQUENCE public.building_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.building_id_seq OWNER TO scheduler_user;

--
-- Name: building_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: scheduler_user
--

ALTER SEQUENCE public.building_id_seq OWNED BY public.building.id;


--
-- Name: departments; Type: TABLE; Schema: public; Owner: scheduler_user
--

CREATE TABLE public.departments (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.departments OWNER TO scheduler_user;

--
-- Name: departments_id_seq; Type: SEQUENCE; Schema: public; Owner: scheduler_user
--

CREATE SEQUENCE public.departments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.departments_id_seq OWNER TO scheduler_user;

--
-- Name: departments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: scheduler_user
--

ALTER SEQUENCE public.departments_id_seq OWNED BY public.departments.id;


--
-- Name: employees; Type: TABLE; Schema: public; Owner: scheduler_user
--

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

--
-- Name: employees_id_seq; Type: SEQUENCE; Schema: public; Owner: scheduler_user
--

CREATE SEQUENCE public.employees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employees_id_seq OWNER TO scheduler_user;

--
-- Name: employees_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: scheduler_user
--

ALTER SEQUENCE public.employees_id_seq OWNED BY public.employees.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: scheduler_user
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO scheduler_user;

--
-- Name: invitation; Type: TABLE; Schema: public; Owner: scheduler_user
--

CREATE TABLE public.invitation (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    department_name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    invited_by character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    token character varying(255) NOT NULL,
    used boolean NOT NULL
);


ALTER TABLE public.invitation OWNER TO scheduler_user;

--
-- Name: invitation_id_seq; Type: SEQUENCE; Schema: public; Owner: scheduler_user
--

CREATE SEQUENCE public.invitation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.invitation_id_seq OWNER TO scheduler_user;

--
-- Name: invitation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: scheduler_user
--

ALTER SEQUENCE public.invitation_id_seq OWNED BY public.invitation.id;


--
-- Name: shift_trades; Type: TABLE; Schema: public; Owner: scheduler_user
--

CREATE TABLE public.shift_trades (
    id bigint NOT NULL,
    approved_by_manager_id bigint,
    completed_at timestamp(6) without time zone,
    reason character varying(255),
    requested_at timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    pickup_employee_id bigint,
    requesting_employee_id bigint NOT NULL,
    shift_id bigint NOT NULL,
    CONSTRAINT shift_trades_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PICKED_UP'::character varying, 'CANCELLED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'POSTED_TO_EVERYONE'::character varying])::text[])))
);


ALTER TABLE public.shift_trades OWNER TO scheduler_user;

--
-- Name: shift_trades_id_seq; Type: SEQUENCE; Schema: public; Owner: scheduler_user
--

CREATE SEQUENCE public.shift_trades_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.shift_trades_id_seq OWNER TO scheduler_user;

--
-- Name: shift_trades_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: scheduler_user
--

ALTER SEQUENCE public.shift_trades_id_seq OWNED BY public.shift_trades.id;


--
-- Name: shifts; Type: TABLE; Schema: public; Owner: scheduler_user
--

CREATE TABLE public.shifts (
    id bigint NOT NULL,
    is_available_for_pickup boolean,
    created_at timestamp(6) without time zone NOT NULL,
    end_time timestamp(6) without time zone NOT NULL,
    notes character varying(255),
    start_time timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    created_by_id bigint NOT NULL,
    department_id bigint NOT NULL,
    employee_id bigint,
    CONSTRAINT shifts_status_check CHECK (((status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying, 'AVAILABLE_FOR_PICKUP'::character varying, 'PENDING'::character varying])::text[])))
);


ALTER TABLE public.shifts OWNER TO scheduler_user;

--
-- Name: shifts_id_seq; Type: SEQUENCE; Schema: public; Owner: scheduler_user
--

CREATE SEQUENCE public.shifts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.shifts_id_seq OWNER TO scheduler_user;

--
-- Name: shifts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: scheduler_user
--

ALTER SEQUENCE public.shifts_id_seq OWNED BY public.shifts.id;


--
-- Name: user_action_log; Type: TABLE; Schema: public; Owner: scheduler_user
--

CREATE TABLE public.user_action_log (
    id bigint NOT NULL,
    action character varying(255) NOT NULL,
    building_id bigint NOT NULL,
    building_name character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    "timestamp" timestamp(6) without time zone NOT NULL,
    user_email character varying(255) NOT NULL,
    user_uuid character varying(255) NOT NULL
);


ALTER TABLE public.user_action_log OWNER TO scheduler_user;

--
-- Name: user_action_log_id_seq; Type: SEQUENCE; Schema: public; Owner: scheduler_user
--

CREATE SEQUENCE public.user_action_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_action_log_id_seq OWNER TO scheduler_user;

--
-- Name: user_action_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: scheduler_user
--

ALTER SEQUENCE public.user_action_log_id_seq OWNED BY public.user_action_log.id;


--
-- Name: building id; Type: DEFAULT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.building ALTER COLUMN id SET DEFAULT nextval('public.building_id_seq'::regclass);


--
-- Name: departments id; Type: DEFAULT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.departments ALTER COLUMN id SET DEFAULT nextval('public.departments_id_seq'::regclass);


--
-- Name: employees id; Type: DEFAULT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.employees ALTER COLUMN id SET DEFAULT nextval('public.employees_id_seq'::regclass);


--
-- Name: invitation id; Type: DEFAULT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.invitation ALTER COLUMN id SET DEFAULT nextval('public.invitation_id_seq'::regclass);


--
-- Name: shift_trades id; Type: DEFAULT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shift_trades ALTER COLUMN id SET DEFAULT nextval('public.shift_trades_id_seq'::regclass);


--
-- Name: shifts id; Type: DEFAULT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shifts ALTER COLUMN id SET DEFAULT nextval('public.shifts_id_seq'::regclass);


--
-- Name: user_action_log id; Type: DEFAULT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.user_action_log ALTER COLUMN id SET DEFAULT nextval('public.user_action_log_id_seq'::regclass);


--
-- Name: building building_pkey; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.building
    ADD CONSTRAINT building_pkey PRIMARY KEY (id);


--
-- Name: departments departments_pkey; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_pkey PRIMARY KEY (id);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);


--
-- Name: employees employees_uuid_unique; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_uuid_unique UNIQUE (uuid);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: invitation invitation_pkey; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.invitation
    ADD CONSTRAINT invitation_pkey PRIMARY KEY (id);


--
-- Name: shift_trades shift_trades_pkey; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shift_trades
    ADD CONSTRAINT shift_trades_pkey PRIMARY KEY (id);


--
-- Name: shifts shifts_pkey; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shifts
    ADD CONSTRAINT shifts_pkey PRIMARY KEY (id);


--
-- Name: invitation uk_ib8hbu2lm2peqra478nwfgrrm; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.invitation
    ADD CONSTRAINT uk_ib8hbu2lm2peqra478nwfgrrm UNIQUE (code);


--
-- Name: departments uk_j6cwks7xecs5jov19ro8ge3qk; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT uk_j6cwks7xecs5jov19ro8ge3qk UNIQUE (name);


--
-- Name: employees uk_j9xgmd0ya5jmus09o0b8pqrpb; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk_j9xgmd0ya5jmus09o0b8pqrpb UNIQUE (email);


--
-- Name: building uk_oyx9p4qp0ot5mw2vdn1qgax00; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.building
    ADD CONSTRAINT uk_oyx9p4qp0ot5mw2vdn1qgax00 UNIQUE (name);


--
-- Name: user_action_log user_action_log_pkey; Type: CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.user_action_log
    ADD CONSTRAINT user_action_log_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: scheduler_user
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: shift_trades fk4pyjpcpt7pip4kt5n163bh7c4; Type: FK CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shift_trades
    ADD CONSTRAINT fk4pyjpcpt7pip4kt5n163bh7c4 FOREIGN KEY (requesting_employee_id) REFERENCES public.employees(id);


--
-- Name: shift_trades fkf6l12c5day1chvr8i7uuvbchw; Type: FK CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shift_trades
    ADD CONSTRAINT fkf6l12c5day1chvr8i7uuvbchw FOREIGN KEY (pickup_employee_id) REFERENCES public.employees(id);


--
-- Name: shifts fkfalfj5kldqkp1mol31gubssrq; Type: FK CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shifts
    ADD CONSTRAINT fkfalfj5kldqkp1mol31gubssrq FOREIGN KEY (department_id) REFERENCES public.departments(id);


--
-- Name: employees fkgy4qe3dnqrm3ktd76sxp7n4c2; Type: FK CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fkgy4qe3dnqrm3ktd76sxp7n4c2 FOREIGN KEY (department_id) REFERENCES public.departments(id);


--
-- Name: shift_trades fkjmmma007a0dvsxg0wccdnd6dp; Type: FK CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shift_trades
    ADD CONSTRAINT fkjmmma007a0dvsxg0wccdnd6dp FOREIGN KEY (shift_id) REFERENCES public.shifts(id);


--
-- Name: shifts fkrj9y63r4yac9rtk1642ckw083; Type: FK CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shifts
    ADD CONSTRAINT fkrj9y63r4yac9rtk1642ckw083 FOREIGN KEY (created_by_id) REFERENCES public.employees(id);


--
-- Name: employees fksi92ll43n1j1qn5xi9l1yv0i8; Type: FK CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fksi92ll43n1j1qn5xi9l1yv0i8 FOREIGN KEY (building_id) REFERENCES public.building(id);


--
-- Name: shifts fktbsbc3nmr4b1vlwtnd944q9u7; Type: FK CONSTRAINT; Schema: public; Owner: scheduler_user
--

ALTER TABLE ONLY public.shifts
    ADD CONSTRAINT fktbsbc3nmr4b1vlwtnd944q9u7 FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

GRANT ALL ON SCHEMA public TO scheduler_user;


--
-- PostgreSQL database dump complete
--

