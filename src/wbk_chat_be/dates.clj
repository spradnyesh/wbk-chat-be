(ns wbk-chat-be.dates
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; parsers and converters

(def formatter (f/formatter "YYYY-MM-dd"))
(defn sql->date [sql] (c/from-date sql))
(defn date->sql [date] (c/to-sql-time date))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; main

(defn today [] (t/today))
(defn n-days-ago [date n] (t/minus date (t/days n)))
(defn unparse [date] (f/unparse formatter date))
(defn parse [string] (f/parse formatter string))
(defn week [date] (t/week-number-of-year date))
(defn year [date] (t/year date))
