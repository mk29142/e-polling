import React, { Component } from 'react';

class BoxList extends Component {
  render() {
    return (
      <ul>
        {this.props.boxes.map(box => (
          <li key={box.id}>{box.text}</li>
        ))}
      </ul>
    );
  }
}

export default BoxList;
