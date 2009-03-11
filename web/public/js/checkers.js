dojo.declare('Game', null, {
  constructor: function(side, board) {
    // keep track of the current position
    this._side = side;
    this._board = board;
    // this._playMap = {};
    // this._undoStack = [];
  },
  getPiece: function(x, y) {
    return this._board[y][x];
  },
  doPlay: function(from, to) {
    // perform the move
    
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

dojo.addOnLoad(function() {
  // maps piece values to image urls
  var piece_images = {
     '1': '/checkers/s/images/pb.png',
     '2': '/checkers/s/images/kb.png',
    '-1': '/checkers/s/images/pr.png',
    '-2': '/checkers/s/images/kr.png'
  };
  
  // get the new position on page load
  var xhr = dojo.xhrGet({
    url: '/checkers/new',
    handleAs: 'json'
  });
  // build the board when the page first loads
  xhr.addCallback(function(res) {
    // set up the game object
    game = new Game(res.side, res.board);
    // create the board and set up targets
    var board = dojo.byId('board');
    // create the table rows
    for (var y = 7; y >= 0; y--) {
      var tr = dojo.create('tr', {}, board);
      // create the table cells
      for (var x = 0; x <= 7; x++) {
        // test that the square is playable
        if ((x + y) % 2 == 0) {
          // create playable square
          var td = dojo.create('td', {'class': 'on'}, tr);
          // look up piece image and insert image tag
          var image = piece_images[game.getPiece(x, y)];
          if (image) dojo.create('img', {'src': image, 'class': 'dojoDndItem'}, td);
          // create the drag/drop source
          new SquareSource(td, {coords: [x, y]});
        } else {
          // create non-playable square
          dojo.create('td', {'class': 'off'}, tr);
        }
      }
    }
  });
  // also update the play map
  xhr.addCallback(function(res) {
    game.updatePlayMap(res.plays);
  });
  
  // listen for drops
  dojo.subscribe('/dnd/drop', function(source) {
    // hook the drop event into the game object
    var target = dojo.dnd.manager().target;
    game.doPlay(source._coords, target._coords);
  });
});
