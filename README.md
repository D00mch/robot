# robot

A Clojure library designed to simplify using java.awt for handling desktop manipulation commands.  
For example, I use robot to change theme in apps: 

[![Clojars Project](https://img.shields.io/clojars/v/robot.svg)](https://clojars.org/robot)

<img src="https://github.com/Liverm0r/robot/blob/master/images/theme.gif" alt="alt text" width="500" height="312">

### Keyboard
```clojure
(require '[robot.core :as r])

;; simulate pressing a single key 
(r/type! :shift)

;; simulate pressing hot keys
(r/hot-keys! [:cmd :space])

;; type whole text 
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
;; put into clipboard
(r/clipboard-put "text to put in clipboard")

;; get from clipboard
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
[Script](https://github.com/Liverm0r/dotfiles/blob/master/clj_scripts/theme/src/theme/core.clj) to change theme (vim, emacs, chrome, slack, osx, telegram)

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
