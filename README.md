# LastFm2SpotifyPlaylist
Gets all played songs from a Last.fm account and searches for the songs in Spotify.
If the song is found it will show the ID in the terminal.

# usage
You need to have keys for Spotify and Last.fm.
I don't rememeber how you get them, but you need to set them to env variables:

```
    LFM_API_KEY
    LFM_API_SECRET
    SPT_CLIENT_ID
    SPT_CLIENT_SECRET
```

The you need to set the Last.fm user name in `LastFm2SpotifyPlaylist.java:22`.

After that you can just run `LastFm2SpotifyPlaylist::main`.