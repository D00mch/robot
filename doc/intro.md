# Introduction to robot

A Clojure library designed to simplify using java.awt for handling desctop manipulation commands

### Keyboard
```clojure
(require '(robot.core))
(require '[robot.core :as r])

;; simulate press a single key 
(r/type! :shift)

;; simulate pressing hot keys
(r/hot-keys! [:cmd :space])

;; type whole text from keyboard 
(r/type-text! "typing this letters")
```

### Mouse
```clojure
;; move cursor to position
(r/mouse-move! 280 1200)

;; getting mouse position
(r/mouse-pos) ;; => [280 1200]

;; simulate mouse click
(r/mouse-click!)

;; simulate mouse wheel
(r/scroll! 10)

;; get pixel color at position
(pixel-color 280 1200)
```

### Clipboard
```clojure
(r/clipboard-put "text to put in clipboard")

(r/clipboard-get-string) ;; => text to put in clipboard
```

### Delay
```clojure
;; you can delay, which will use thread/sleep under the hood
(r/sleep 50)

;; you can also pass delays inside typing functions
(r/type! :k 50) ;; passing millis between press and release

;; same with mouse 
(r/mouse-click! 100) 
```

## Examples

[Script](https://github.com/Liverm0r/dotfiles/blob/master/clj_scripts/trutenko/src/trutenko/core.clj) to notify developer about standup in Slack
