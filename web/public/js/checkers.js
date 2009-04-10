// the global game object - will be defined by addOnLoad
var game;

dojo.require('dojo.dnd.Source');
dojo.declare('SquareSource', dojo.dnd.Source, {
  singular: true, // don't allow multiple selection regardless of key state
  autoSync: true, // always sync up node list with square contents
  constructor: function(node, x, y) {
    // keep track of the coords for this square
    this.x = x;
    this.y = y;
  },
  copyState: function() {
    // never allow pieces to be copied
    return false;
  },
  checkAcceptance: function(source) {
    // whether the drop is a valid move
    return game.isPlay(source.x, source.y, this.x, this.y);
  }
});

dojo.declare('Game', null, {
  _pieceImages: {
     '1': 'pb.png',
     '2': 'kb.png',
    '-1': 'pr.png',
    '-2': 'kr.png'
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
          var p = this._board[y][x];
          if (p != 0) {
            // create image tag for piece
            var img = dojo.create('img', {'class': 'dojoDndItem'}, td);
            img.src = '/s/images/' + this._pieceImages[p];
          }
          // create the drag/drop source
          new SquareSource(td, x, y);
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
    return dojo.query('> img', td)[0];
  },
  _setImg: function(x, y, p) {
    var tr  = dojo.query('#board tr')[7 - y];
    var td  = dojo.query('> td', tr)[x];
    var img = dojo.query('> img', td)[0];
    if (p == 0) {
      if (img) dojo.destroy(img);
    } else {
      if (!img) img = dojo.create('img', {'class': 'dojoDndItem'}, td);
      img.src = '/s/images/' + this._pieceImages[p];
    }
  },
  constructor: function() {
    // get a new game from the server
    dojo.xhrGet({
      url: '/new',
      handleAs: 'json',
      load: dojo.hitch(this, function(res) {
        // set up the game attributes
        this._side = res.side;
        this._board = res.board;
        this._setupBoard();
        this.updatePlayMap(res.plays);
      })
    });
    // listen for drops
    dojo.subscribe('/dnd/drop', this, function(source) {
      var target = dojo.dnd.manager().target;
      this.handleDrop(source.x, source.y, target.x, target.y);
    });
  },
  handleDrop: function(x, y, nx, ny) {
    // just in case, we'll check again
    var playMap = this.isPlay(x, y, nx, ny);
    if (!playMap) return;
    this.movePiece(x, y, nx, ny, true);
    // keep track of this move
    if (this._plays.length == 0) {
      this._plays.push(x);
      this._plays.push(y);
    }
    this._plays.push(nx);
    this._plays.push(ny);
    // see if any plays remain
    this._playMap = {};
    if (playMap == true) {
      // move complete
      this.onPlayComplete();
    } else if (playMap) {
      // still your move
      this._playMap[nx + ',' + ny] = playMap;
    }
  },
  movePiece: function(x, y, nx, ny, dropped) {
    // move the piece
    var p = this._board[y][x];
    this._board[y][x] = 0;
    this._board[ny][nx] = p;
    if (!dropped) {
      this._setImg(x, y, 0);
      this._setImg(nx, ny, p);
    }
    if (Math.abs(nx - x) == 2) {
      // remove the jumped piece
      var mx = (x + nx) / 2;
      var my = (y + ny) / 2
      this._board[my][mx] = 0;
      this._setImg(mx, my, 0);
    }
    // TODO: promote piece
  },
  moveAll: function(move) {
    this.movePiece(move[0], move[1], move[2], move[3]);
    if (move.length > 4) {
      this.moveAll(move.slice(2));
    }
  },
  onPlayComplete: function() {
    // get next move from the server
    dojo.xhrGet({
      url: '/play',
      handleAs: 'json',
      content: {move: dojo.toJson(this._plays)},
      load: dojo.hitch(this, function(res) {
        this.moveAll(res.move);
        this.updatePlayMap(res.plays);
      })
    });
  },
  isPlay: function(x, y, nx, ny) {
    // whether this is a valid move
    var fromMap = this._playMap[x + ',' + y];
    if (fromMap) return fromMap[nx + ',' + ny];
  },
  updatePlayMap: function(plays) {
    // _playMap is a mapping over the tree of legal moves:
    //   this._playMap['2,2']['3,3'] <==> true
    //   this._playMap['2,2']['4,4']['6,6'] <==> true
    // - a terminal move yields true
    // - a partial move yields a mapping over the remaining moves
    this._playMap = this._playsToMap(plays);
    this._plays = [];
  },
  _playsToMap: function(plays) {
    // helper
    var playMap = {};
    dojo.forEach(plays, function(play) {
      playMap[play[0] + ',' + play[1]] = (play.length == 2 ?
        true :
        this._playsToMap(play.slice(2)));
    }, this);
    return playMap;
  }
});

dojo.addOnLoad(function() {
  game = new Game();
});
