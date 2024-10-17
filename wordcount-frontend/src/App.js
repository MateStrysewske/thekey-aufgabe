import './App.css';
import { useEffect, useState, useMemo } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import MaterialTable from './MaterialTable.js';
import { AppBar, Typography } from '@mui/material'

function App() {

  const BACKEND_ENDPOINT = 'http://localhost:8080/ws';
  const WEBSOCKET_ENDPOINT = '/topic/wordcounts';

  /**
   * Stores the word counts JSON received via websocket for future comparison
   */
  let currentWordCountsJson;

  const columns = useMemo(() => [
    { Header: 'Word', accessor: 'word' },
    { Header: 'Count', accessor: 'count' },
  ], []);

  const [data, setData] = useState([]);

  /**
   * Converts a map of words and their associated counts to data that can be 
   * displayed in the table, and sorts it by count.
   */
  const convertMapToData = map => {
    const data = [];
    for (const [key, value] of Object.entries(map)) {
      data.push({ word: key, count: value });
    }
    data.sort((a, b) => b.count - a.count);
    return data;
  }

  // Connect to WEBSOCKET_ENDPOINT to subscribe to pushed word count maps from blog posts,
  // and update the table if an updated word count map is pushed.
  useEffect(() => {
    const socket = new SockJS(BACKEND_ENDPOINT);
    const stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        console.log('Connected to blog entries websocket!');
        stompClient.subscribe(WEBSOCKET_ENDPOINT, message => {
          if (!currentWordCountsJson || JSON.stringify(currentWordCountsJson) !== JSON.stringify(message.body)) {
            const wordCounts = JSON.parse(message.body);
            setData(convertMapToData(wordCounts));
            currentWordCountsJson = message.body;
          }
        });
      },
      onStompError: frame => {
        console.error('STOMP error: ', frame);
      }
    });

    stompClient.activate();

    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, []);

  return (
    <div className="App">
      <AppBar position="fixed" sx={{ padding: 2 }}>
        <Typography variant="h6" color="inherit" component="div">
          Blog Post Word Counts
        </Typography>
      </AppBar>
      <div class="table-container">
        <MaterialTable columns={columns} data={data} />
      </div>
    </div>
  );
}

export default App;
