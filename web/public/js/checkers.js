dojo.require('dojo.dnd.Source');

dojo.declare('SquareSource', dojo.dnd.Source, {
  // don't allow multiple selection regardless of key state
  singular: true,
  
  // always sync up node list with square contents
  autoSync: true,
  
  // should contain a mapping for each legal move, such that:
  //   this.play_map['2,2']['3,3'] <==> true
  // if the move from 2,2 => 3,3 is allowed
  play_map: {},
  
  constructor: function(node, params) {
    // keep track of the coords managed by this square, so we can test for it in checkAcceptance
    this._coords = params.coords;
  },
  
  copyState: function() {
    // don't allow pieces to be copied regardless of key state
    return false;
  },
  
  checkAcceptance: function(source) {
    // return whether the piece wanting to be dropped on us represents a valid move
    return (this.play_map[source._coords] || dojo.dnd._empty)[this._coords];
  }
});

dojo.addOnLoad(function() {
  // set up all drop source/targets
  for (var y = 0; y < 8; y++) {
    for (var x = 0; x < 8; x++) {
      var tr = dojo.query('#board tr')[7 - y];
      var td = dojo.query('> td', tr)[x];
      
      // if there is a piece here, make it a dnd item
      var img = dojo.query('> img', td)[0];
      if (img) dojo.addClass(img, 'dojoDndItem');
      
      // create the source
      var src = new SquareSource(td, {coords: [x, y].toString()});
    }
  }
  
  // set up play_map based on moves from server
  for (var i = 0; i < plays.length; i++) {
    var play = plays[i];
    var from = SquareSource.prototype.play_map[play[0]] = {};
    for (var j = 1; j < play.length; j++) {
      from[play[j][0]] = true;
    }
  }
});
