// the global game object - will be defined by addOnLoad
var game;

dojo.require('dojo.dnd.Source');
dojo.declare('SquareSource', dojo.dnd.Source, {
  singular: true, // don't allow multiple selection regardless of key state
  autoSync: true, // always sync up node list with square contents
  constructor: function(node, params) {
    // keep track of the coords for this square
    this._coords = params.coords;
  },
  copyState: function() {
    // never allow pieces to be copied
    return false;
  },
  checkAcceptance: function(source) {
    // whether the drop is a valid move
    return game.isPlay(source._coords, this._coords);
  }
});

dojo.declare('Game', null, {
  _pieceImages: {
     '1': '/checkers/s/images/pb.png',
     '2': '/checkers/s/images/kb.png',
    '-1': '/checkers/s/images/pr.png',
    '-2': '/checkers/s/images/kr.png'
  },
  _setupBoard: function() {
    // create the board and set up targets
    var table = dojo.byId('board');
    // create the table rows
    for (var y = 7; y >= 0; y--) {
      var tr = dojo.create('tr', {}, table);
      // create the table cells
      for (var x = 0; x <= 7; x++) {
        // test that the square is playable
        if ((x + y) % 2 == 0) {
          // create playable square
          var td = dojo.create('td', {'class': 'on'}, tr);
          // look up piece image and insert image tag
          var image = this._pieceImages[this._board[y][x]];
          if (image) dojo.create('img', {'src': image, 'class': 'dojoDndItem'}, td);
          // create the drag/drop source
          new SquareSource(td, {coords: [x, y]});
        } else {
          // create non-playable square
          dojo.create('td', {'class': 'off'}, tr);
        }
      }
    }
  },
  _getImg: function(x, y) {
    var tr = dojo.query('#board tr')[7 - y];
    var td = dojo.query('> td', tr)[x];
    var nodes = dojo.query('> img', td);
    if (nodes.length > 0) return nodes[0];
  },
  constructor: function(side, board) {
    // keep track of the current position
    this._side = side;
    this._board = board;
    this._plays = [];
    this._setupBoard();
    // listen for drops
    dojo.subscribe('/dnd/drop', this, function(source) {
      var target = dojo.dnd.manager().target;
      this.doPlay(source._coords, target._coords);
    });
  },
  doPlay: function(from, to) {
    // just in case, we'll check again
    var playMap = this.isPlay(from, to);
    if (!playMap) return;
    // keep track of this move
    if (this._plays.length == 0)
      this._plays.push(from);
    this._plays.push(to);
    // move the piece
    var x  = from[0];
    var y  = from[1];
    var nx = to[0];
    var ny = to[1];
    var p  = this._board[y][x];
    this._board[y][x] = 0;
    this._board[ny][nx] = p;
    if (Math.abs(nx - x) == 2) {
      // remove the jumped piece
      var mx = (x + nx) / 2;
      var my = (y + ny) / 2
      this._board[my][mx] = 0;
      dojo.destroy(this._getImg(mx, my));
    }
    // fire an event
    if (playMap == true) {
      // move complete
      dojo.publish('/game/move/complete', [this._plays]);
    } else if (playMap) {
      // still your move
      this._playMap = {};
      this._playMap[to] = playMap;
      dojo.publish('/game/move/partial', [this._plays]);
    }
  },
  undoPlay: function(from, to, jumped) {
    // undo part of a move
  },
  isPlay: function(from, to) {
    // whether this is a valid move
    var fromMap = this._playMap[from];
    if (fromMap) return fromMap[to];
  },
  updatePlayMap: function(plays) {
    // _playMap is a mapping over the tree of legal moves:
    //   this._playMap['2,2']['3,3'] <==> true
    //   this._playMap['2,2']['4,4']['6,6'] <==> true
    // - a terminal move yields true
    // - a partial move yields a mapping over the remaining moves
    this._playMap = this._playsToMap(plays);
  },
  _playsToMap: function(plays) {
    // helper
    var playMap = {};
    dojo.forEach(plays, function(play) {
      playMap[play[0]] = (play.length == 1 ? true : this._playsToMap(play.slice(1)));
    }, this);
    return playMap;
  }
});

dojo.addOnLoad(function() {
  // get the new position on page load
  var xhr = dojo.xhrGet({
    url: '/checkers/new',
    handleAs: 'json',
    load: function(res) {
      // set up the global game object
      game = new Game(res.side, res.board);
      game.updatePlayMap(res.plays);
    }
  });
});
