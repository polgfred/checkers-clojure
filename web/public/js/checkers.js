dojo.declare('Game', null, {
  constructor: function(side, board) {
    // keep track of the current position
    this._side = side;
    this._board = board;
  },
  doMove: function(from, to) {
    // perform the move
  }
});

dojo.require('dojo.dnd.Source');

dojo.declare('SquareSource', dojo.dnd.Source, {
  singular: true, // don't allow multiple selection regardless of key state
  autoSync: true, // always sync up node list with square contents
  constructor: function(node, params) {
    // keep track of the coords for this square
    this._coords = params.coords;
    this._coordsKey = params.coords.toString();
  },
  copyState: function() {
    // never allow pieces to be copied
    return false;
  },
  checkAcceptance: function(source) {
    // return whether the drop is a valid move
    // see _playMap below
    return (this._playMap[source._coordsKey] || dojo.dnd._empty)[this._coordsKey];
  },
  // _playMap should contain a mapping for each legal move, such that:
  //   this._playMap['2,2']['3,3'] <==> true
  // if the move from 2,2 => 3,3 is allowed
  updatePlayMap: function(plays) {
    // needs to be shared across all instances
    SquareSource.prototype._playMap = {};
    // iterate through plays array from server and build the map
    for (var i = 0; i < plays.length; i++) {
      var play = plays[i];
      var fromMap = this._playMap[play[0]] = {};
      for (var j = 1; j < play.length; j++) {
        fromMap[play[j][0]] = true;
      }
    }
  }
});

// the global game object
var game;

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
          var td = dojo.create('td', {'class': 'on'}, tr);
          // look up piece image and insert image tag
          var p = res.board[y][x];
          var src = piece_images[p];
          if (src) dojo.create('img', {'src': src, 'class': 'dojoDndItem'}, td);
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
    SquareSource.prototype.updatePlayMap(res.plays);
  });
  
  // listen for drops
  dojo.subscribe('/dnd/drop', function(source) {
    // hook the drop event into the game object
    var target = dojo.dnd.manager().target;
    game.doMove(source._coords, target._coords);
  });
});
