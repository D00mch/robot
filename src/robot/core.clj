(ns robot.core
  (:gen-class)
  (:require
   [clojure.java.shell :use [sh]]
   [clojure.string :as str])
  (:import
   (java.awt
    Desktop
    MouseInfo
    Rectangle
    Robot
    Toolkit)
   (java.awt.datatransfer
    Clipboard
    DataFlavor
    StringSelection
    Transferable)
   (java.awt.event InputEvent KeyEvent)
   (java.awt.im InputContext)
   [java.net URI]))

(comment
  (set! *warn-on-reflection* true))

(def ^{:private true} key-events-map
  {:a         KeyEvent/VK_A    :b KeyEvent/VK_B     :c KeyEvent/VK_C :d KeyEvent/VK_D :e KeyEvent/VK_E
   :f         KeyEvent/VK_F    :g KeyEvent/VK_G     :h KeyEvent/VK_H :i KeyEvent/VK_I :j KeyEvent/VK_J
   :k         KeyEvent/VK_K    :l KeyEvent/VK_L     :m KeyEvent/VK_M :n KeyEvent/VK_N :o KeyEvent/VK_O
   :p         KeyEvent/VK_P    :q KeyEvent/VK_Q     :r KeyEvent/VK_R :s KeyEvent/VK_S :t KeyEvent/VK_T
   :u         KeyEvent/VK_U    :v KeyEvent/VK_V     :w KeyEvent/VK_W :x KeyEvent/VK_X :y KeyEvent/VK_Y
   :z         KeyEvent/VK_Z
   :1         KeyEvent/VK_1    :2 KeyEvent/VK_2     :3 KeyEvent/VK_3 :4 KeyEvent/VK_4 :5 KeyEvent/VK_5
   :6         KeyEvent/VK_6    :7 KeyEvent/VK_7     :8 KeyEvent/VK_8 :9 KeyEvent/VK_9 :0 KeyEvent/VK_0
   :cmd       KeyEvent/VK_META :meta KeyEvent/VK_META
   :shift     KeyEvent/VK_SHIFT
   :alt       KeyEvent/VK_ALT
   :esc       KeyEvent/VK_ESCAPE
   :enter     KeyEvent/VK_ENTER
   :back      KeyEvent/VK_BACK_SPACE                :backspace KeyEvent/VK_BACK_SPACE
   :bq        KeyEvent/VK_BACK_QUOTE                            ; back quote
   :quote     KeyEvent/VK_QUOTE
   :tab       KeyEvent/VK_TAB
   :caps      KeyEvent/VK_CAPS_LOCK
   :ctrl      KeyEvent/VK_CONTROL
   :space     KeyEvent/VK_SPACE
   :win       KeyEvent/VK_WINDOWS
   :page-down KeyEvent/VK_PAGE_DOWN 
   :page-up   KeyEvent/VK_PAGE_UP 
   :home      KeyEvent/VK_HOME
   :end       KeyEvent/VK_END
   :f1        KeyEvent/VK_F1   :f2 KeyEvent/VK_F2   :f3 KeyEvent/VK_F3   :f4 KeyEvent/VK_F4
   :f5        KeyEvent/VK_F5   :f6 KeyEvent/VK_F6   :f7 KeyEvent/VK_F7   :f8 KeyEvent/VK_F8
   :f9        KeyEvent/VK_F9   :f10 KeyEvent/VK_F10 :f11 KeyEvent/VK_F11 :f12 KeyEvent/VK_F12
   :left      KeyEvent/VK_LEFT :right KeyEvent/VK_R :up KeyEvent/VK_UP   :down KeyEvent/VK_DOWN})

;; KEYBOARD-API
(def ^Robot robot (Robot.))

(defn sleep [millis]
  (.delay robot millis))

(defn- keys->key-events [keys]
  (map #(% key-events-map) keys))

(defn type! [key & [delay]]
  (let [key (if (number? key) key (key key-events-map))]
    (doto robot
      (.keyPress key)
      (.delay (or delay 40))
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
                code (cond
                       (< 96 code 123) (- code 32)
                       :else           code)]]
    (case code
      33 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_1])
      34 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_QUOTE])
      35 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_3])
      36 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_4])
      37 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_5])
      38 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_7])
      39 (type! KeyEvent/VK_QUOTE)
      40 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_9])
      41 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_0])
      42 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_8])
      91 (type! KeyEvent/VK_OPEN_BRACKET)
      93 (type! KeyEvent/VK_CLOSE_BRACKET)
      94 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_6])
      96 (type! KeyEvent/VK_BACK_QUOTE)
      58 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_SEMICOLON])
      63 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_SLASH])
      64 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_2])
      123 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_OPEN_BRACKET])
      125 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_CLOSE_BRACKET])
      126 (hot-keys! [KeyEvent/VK_SHIFT KeyEvent/VK_BACK_QUOTE])
      (doto robot
        (.delay (or delay-before-press 70))
        (.keyPress code)
        (.delay (or delay-before-release 0))
        (.keyRelease code)))))

;; INFO

(defn get-key-name [i]
  (KeyEvent/getKeyText i))

(defn get-my-keyboard []
  (into (sorted-map)
        (for [i (range 100000)
              :let [text (get-key-name i)]
              :when (not (.contains ^String text "Unknown keyCode: "))]
          [i text])))

(defn get-screen-size []
  (let [screen (.. Toolkit getDefaultToolkit getScreenSize)]
    [(.width screen)
     (.height screen)]))

(defn get-current-layout []
  (.getLocale (InputContext/getInstance)))

;; MOUSE

(defn mouse-click!
  "mouse-button: #{:mouse1, :mouse2, :mouse3}"
  ([]
   (mouse-click! :mouse1))
  ([mouse-button & [delay]]
   (let [btn (case mouse-button
               :mouse1 InputEvent/BUTTON1_DOWN_MASK
               :mouse2 InputEvent/BUTTON2_DOWN_MASK
               :mouse3 InputEvent/BUTTON3_DOWN_MASK)]
     (doto robot
       (.mousePress btn)
       (.delay (or delay 70))
       (.mouseRelease btn)))))

(defn mouse-pos "returns mouse position [x, y]" []
  (let [mouse-info (.. MouseInfo getPointerInfo getLocation)]
    [(. mouse-info x) (. mouse-info y)]))

(def get-mouse-pos mouse-pos)

(defn mouse-move!
  ([[x y]] (mouse-move! x y))
  ([x y] (.mouseMove robot x y)))

(defn scroll! [i] (.mouseWheel ^Robot robot i))

;; COLORS

(defn pixel-color
  "Don't use this function in loops, it's slow.
  If you need range, use `pixel-rgb-range`"
  ([]
   (pixel-color (mouse-pos)))
  ([[x y]]
   (pixel-color x y))
  ([x y]
   (.getPixelColor robot x y)))

(defn pixel-argb-int
  "Don't use this function in loops, it's slow.
  If you need range, use `pixel-rgb-range(-ver|-hor)`"
  [x y]
  (.getRGB (.createScreenCapture robot (Rectangle. x y 1 1)) 0 0))

(defrecord ARGB [alpha red green blue])

(defn int->argb
  "Get integer argb, retuns ARGB record {:keys [apha red green blue]}
   Don't use in loops, consider `pixel-rgb-range(-ver|-hor) ` instead."
  [color-int]
  (->ARGB (bit-and (bit-shift-right color-int 24) 255)
          (bit-and (bit-shift-right color-int 16) 255)
          (bit-and (bit-shift-right color-int 8) 255)
          (bit-and color-int 255)))

(defn pixel-argb
  "Don't use this function in loops, it's slow.
  If you need range, use `pixel-rgb-range(-ver|-hor)`"
  ([]
   (pixel-argb (mouse-pos)))
  ([[x y]]
   (pixel-argb x y))
  ([x y]
   (int->argb (pixel-argb-int x y))))

(defn pixel-rgb-range
  "Pass `map-fn` to modify each pixel value (argb by default) "
  ([x y width height]
   (pixel-rgb-range x y width height identity))
  ([x y width height map-fn]
   (let [rec (.createScreenCapture robot (Rectangle. x y width height))]
     (for [yn (range height)]
       (for [xn (range width)]
         (map-fn (.getRGB rec xn yn)))))))

(defn pixel-rgb-range-hor
  "Horizontal range. See `pixel-rgb-range` docs"
  ([[x y] width]
   (pixel-rgb-range-hor x y width identity))
  ([x y width]
   (pixel-rgb-range-hor x y width identity))
  ([x y width map-fn]
   (first (pixel-rgb-range x y width 1 map-fn))))

(defn pixel-rgb-range-ver
  "Horizontal range. See `pixel-rgb-range` docs"
  ([[x y] height]
   (pixel-rgb-range-ver x y height identity))
  ([x y height]
   (pixel-rgb-range-ver x y height identity))
  ([x y height map-fn]
   (let [rec (.createScreenCapture robot (Rectangle. x y 1 height))]
     (for [n (range height)]
       (map-fn (.getRGB rec 0 n))))))

;; CLIPBOARD

(def ^Clipboard clipboard (.. Toolkit getDefaultToolkit getSystemClipboard))

(defn clipboard-put! [^String s]
  (try
    (.setContents clipboard (StringSelection. s) nil)
    (catch Exception e
      (sleep 20)
      (.setContents clipboard (StringSelection. s) nil))))

(defn clipboard-get-string
  "returns string from buffer or nil"
  []
  (let [^Transferable content (try (.getContents clipboard nil)
                                   (catch Throwable e
                                     (sleep 20)
                                     (.getContents clipboard nil)))
        has-text              (and (some? content)
                                   (.isDataFlavorSupported content DataFlavor/stringFlavor))]
    (when has-text
      (try
        (.getTransferData content DataFlavor/stringFlavor)
        (catch Exception e (.printStackTrace e))))))

;; LAUNCH

(defn launch-uri! [uri-str]
  (.browse (Desktop/getDesktop) (URI. uri-str)))
