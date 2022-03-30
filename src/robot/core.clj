(ns robot.core
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.java.shell :use [sh]])
  (:import (java.awt Robot Toolkit MouseInfo)
           (java.awt.datatransfer Clipboard StringSelection
                                  Transferable DataFlavor)
           (java.awt.event KeyEvent InputEvent)))

(comment
  (set! *warn-on-reflection* true))

(def ^{:private true} key-events-map
  {:a     KeyEvent/VK_A :b KeyEvent/VK_B :c KeyEvent/VK_C :d KeyEvent/VK_D :e KeyEvent/VK_E
   :f     KeyEvent/VK_F :g KeyEvent/VK_G :h KeyEvent/VK_H :i KeyEvent/VK_I :j KeyEvent/VK_J
   :k     KeyEvent/VK_K :l KeyEvent/VK_L :m KeyEvent/VK_M :n KeyEvent/VK_N :o KeyEvent/VK_O
   :p     KeyEvent/VK_P :q KeyEvent/VK_Q :r KeyEvent/VK_R :s KeyEvent/VK_S :t KeyEvent/VK_T
   :u     KeyEvent/VK_U :v KeyEvent/VK_V :w KeyEvent/VK_W :x KeyEvent/VK_X :y KeyEvent/VK_Y
   :z     KeyEvent/VK_Z
   :1     KeyEvent/VK_1 :2 KeyEvent/VK_2 :3 KeyEvent/VK_3 :4 KeyEvent/VK_4 :5 KeyEvent/VK_5
   :6     KeyEvent/VK_6 :7 KeyEvent/VK_7 :8 KeyEvent/VK_8 :9 KeyEvent/VK_9 :0 KeyEvent/VK_0
   :cmd   KeyEvent/VK_META :meta KeyEvent/VK_META
   :shift KeyEvent/VK_SHIFT
   :alt   KeyEvent/VK_ALT
   :esc   KeyEvent/VK_ESCAPE
   :enter KeyEvent/VK_ENTER
   :back  KeyEvent/VK_BACK_SPACE
   :bq    KeyEvent/VK_BACK_QUOTE                            ; back quote
   :quote KeyEvent/VK_QUOTE
   :tab   KeyEvent/VK_TAB
   :caps  KeyEvent/VK_CAPS_LOCK
   :ctrl  KeyEvent/VK_CONTROL
   :space KeyEvent/VK_SPACE
   :f1    KeyEvent/VK_F1 :f2 KeyEvent/VK_F2 :f3 KeyEvent/VK_F3 :f4 KeyEvent/VK_F4
   :f5    KeyEvent/VK_F5 :f6 KeyEvent/VK_F6 :f7 KeyEvent/VK_F7 :f8 KeyEvent/VK_F8
   :f9    KeyEvent/VK_F9 :f10 KeyEvent/VK_F10 :f11 KeyEvent/VK_F11 :f12 KeyEvent/VK_F12
   :left  KeyEvent/VK_LEFT :right KeyEvent/VK_R :up KeyEvent/VK_UP :down KeyEvent/VK_DOWN
   })


;; KEYBOARD-API
(def ^Robot robot (Robot.))

(defn sleep [time]
  (.delay robot 300))

(defn- keys->key-events [keys]
  (map #(% key-events-map) keys))

(defn type! [key & [delay]]
  (let [key (if (number? key) key (key key-events-map))]
    (doto robot
      (.delay (or delay 40))
      (.keyPress key)
      (.keyRelease key))))

(defn hot-keys! "takes seq of ints (KeyEvent) or :keys"
  [keys & [delay-between-press delay-before-release]]
  (let [keys (if (number? (first keys)) keys (keys->key-events keys))]
    (doseq [key keys]
      (doto robot
        (.keyPress key)
        (.delay (or delay-between-press 10))))
    (.delay robot (or delay-before-release 100))
    (doseq [key (reverse keys)] (.keyRelease robot key))))

(defn type-text! [^String s & [delay-before-press delay-before-release]]
  (doseq [byte (.getBytes s)
          :let [code (int byte)
                code (if (< 96 code 123) (- code 32) code)]]
    (doto robot
      (.delay (or delay-before-press 70))
      (.keyPress code)
      (.delay (or delay-before-release 0))
      (.keyRelease code))))

;; MOUSE

(defn mouse-click! [& [delay]]
  (doto robot
    (.mousePress InputEvent/BUTTON1_DOWN_MASK)
    (.delay (or delay 70))
    (.mouseRelease InputEvent/BUTTON1_DOWN_MASK)))

(defn mouse-pos "returns mouse position [x, y]" []
  (let [mouse-info (.. MouseInfo getPointerInfo getLocation)]
    [(. mouse-info x) (. mouse-info y)]))

(defn mouse-move!
  ([[x y]] (mouse-move! x y))
  ([x y] (.mouseMove robot x y)))

(defn scroll! [i] (.mouseWheel ^Robot robot i))

(defn pixel-color [x y] (.getPixelColor robot x y))


;; CLIPBOARD

(def ^Clipboard clipboard (.. Toolkit getDefaultToolkit getSystemClipboard))

(defn clipboard-put! [^String s]
  (.setContents clipboard (StringSelection. s) nil))

(defn clipboard-get-string "returns string from buffer or nil" []
  (let [^Transferable content (.getContents clipboard nil)
        has-text              (and (some? content)
                                   (.isDataFlavorSupported content DataFlavor/stringFlavor))]
    (when has-text
      (try
        (.getTransferData content DataFlavor/stringFlavor)
        (catch Exception e (.printStackTrace e))))))

;; INFO

(defn get-key-name [i]
  (KeyEvent/getKeyText i))

(defn get-my-keyboard []
  (into (sorted-map)
        (for [i (range 100000)
              :let [text (get-key-name i)]
              :when (not (.contains ^String text "Unknown keyCode: "))]
          [i text])))
