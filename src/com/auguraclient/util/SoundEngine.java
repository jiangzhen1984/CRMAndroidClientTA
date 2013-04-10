package com.auguraclient.util;

import java.io.IOException;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

/***
 * 
 * @author Swapnil Desai
 * All sounds and background musics played here
 *
 */
public class SoundEngine 
{
	// effects are sounds that less than 5 seconds, better in 3 seconds
	IntMap<Integer> effectsMap = new IntMap<Integer>();
	//All background musics
	IntMap<Integer> streamsMap = new IntMap<Integer>();
	
	//player for background music
	MediaPlayer mp=null;
	//sound pool for effects
	SoundPool sp = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
	int lastSndId = -1;
	Float prevEffectsVolume = 0.5f;
	Float prevSoundsVolume = 0.5f;
	Float effectsVolume = 0.5f;
	Float soundsVolume = 0.5f;
	//boolean mute = false;
	boolean effect_mute=false;
	boolean sound_mute=false;
	
    static SoundEngine _sharedEngine = null;

    /***
     * 
     * @return Create singlton class so complete applications will use same sounds no need to preload and release every time can be released only on application exit
     */
    public static SoundEngine sharedEngine()
    {
        synchronized(SoundEngine.class) {
            if (_sharedEngine == null) {
                _sharedEngine = new SoundEngine();
            }
        }
        return _sharedEngine;
    }

    
    private SoundEngine()
    {
    	
    }
    /***
     * Clear sound engine while exiting and release resources used by player so can be used by other applications
     */
    public static void purgeSharedEngine() {
        synchronized(SoundEngine.class) {
            _sharedEngine = null;
        }
    }
    
    /***
     * 
     * @param set volume for effect
     */
    public void setEffectsVolume(Float volume) 
    {
    	if (effect_mute)
    		return;
    	
    	effectsVolume = volume;
    }
    /***
     * 
     * @return volume set for effect
     */
    public Float getEffectsVolume() 
    {
    	return effectsVolume;
    }
    /***
     * 
     * @param volume:Volume for the player to be set
     */
    public void setSoundVolume(Float volume) 
    {
    	if (sound_mute)
    		return;
    	if(volume==null)
    		return;
    	soundsVolume = volume;
    	if(mp != null)
    	{
    		mp.setVolume(soundsVolume, soundsVolume);
    	}
    }
    /***
     * 
     * @return get current sound volume
     */
    public Float getSoundsVolume() 
    {
    	return soundsVolume;
    }
    
  
    /***
     * mute effect
     */
    public void muteEffect() 
    {
    	if (effect_mute)
    		return;
    	prevEffectsVolume = effectsVolume;
    	prevSoundsVolume = soundsVolume;
    	effectsVolume = 0f;
    	effect_mute = true;
    }
    /***
     * Mute sound
     */
    public void muteSound() 
    {
    	if (sound_mute)
    		return;
    	setSoundVolume(0f);
    	sound_mute = true;
    }
    /***
     * Unmute sound
     */
    public void unmute_effect() {
    	if (!effect_mute)
    		return;
    	
    	effectsVolume = prevEffectsVolume;
    	effect_mute = false;
    }
    
    /***
     * unmute sound
     */
    public void unmute_sound() {
    	if (!sound_mute)
    		return;
    	sound_mute = false;
    	setSoundVolume(prevSoundsVolume);
    }
    
    /***
     * 
     * @return Check if effect is mute
     */
    public boolean isEffect_Mute() {
    	return effect_mute;
    }

    /***
     * 
     * @return check if sound is mute
     */
    public boolean isSound_Mute() {
    	return sound_mute;
    }
    /***
     * Preload all sound effects sometime sound wont get played if it is not loaded before
     * @param app 
     * @param resId
     */
	public void preloadEffect(Context app, int resId)
	{
		synchronized(effectsMap) {
			Integer sndId = effectsMap.get(resId);
			if (sndId != null)
				return;
			
			sndId = sp.load(app, resId, 0);
			effectsMap.put(resId, sndId);
		}
	}
		
	public void addLog(String log)
	{
		Log.d("SOUNDENGINE",log);
	}
	
	/***
	 * 
	 * @param app=application context
	 * @param resId=id of the file to be played
	 */
	public void playEffect(Context app, int resId) {
		Integer sndId = -1;
		synchronized (effectsMap) 
		{
			sndId = effectsMap.get(resId);
			if (sndId == null) 
			{
				//loads sound file from raw folder.
				//context
				//resID : id of the resource from raw folder
				//priority:currently not in use use as 0
				//returns sound id which can be used to play sound
				sndId = sp.load(app, resId, 0);
				effectsMap.put(resId, sndId);
			}
		}
		//play (int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate)
		//sound id:returned from the load function
		//left volume value ranges from 0 to 1
		//right volume
		//priority:higher number higher the priority
		//-1 loop forever
		//other number how many times ex. 1 means sound will be played twice
		//sampling rate 0.5 to 2
		int streamId = sp.play(sndId,1f,1f, 0, 0, 1.0f);
		if (effectsVolume != null)
		{
			//setVolume(id,leftvolume,rightvolume);
			sp.setVolume(streamId, effectsVolume, effectsVolume);
		}
		streamsMap.put(resId, streamId);
	}
	
	/***
	 * 
	 * @param app=application context
	 * @param resId=Res id of the effect to be played
	 */
	public void stopEffect(Context app, int resId) 
	{
		Integer sid = streamsMap.get(resId);
		if (sid != null) {
			sp.stop(sid);
		}
	}
	
	
	/**
	 * 
	 * @param ctxt=Application context
	 * @param resId=Res id of the file to be played
	 * @param loop=true if we need to loop the sound
	 */
	public void playSound(Context ctxt, int resId, boolean loop) {
		
		
			if(sound_mute)
			{
				return;
			}
		
			mp=null;
			if (mp == null) 
			{
				mp = MediaPlayer.create(ctxt, resId);
				
				// failed to create
				if(mp == null)
					return;
				
				

				try {
					mp.prepare();
				} catch (IllegalStateException e) 
				{
				} catch (IOException e) 
				{
				}
			}
		
		

		if (soundsVolume != null)
			mp.setVolume(soundsVolume, soundsVolume);
		mp.start();

		if (loop)
			mp.setLooping(true);
	}
	
	/***
	 * Pause backround sound
	 */
	public void pauseSound()
	{
	    try
	    {
	    	if(mp!=null)
			{
				mp.pause();
				mp.stop();
				mp.release();
				mp=null;
			}
	    }catch(Exception e)
	    {
	    	
	    }
		
	}
	
	
	/***
	 * Release all sound effects from sound pool
	 */
	public void realesAllEffects() 
	{
		if(sp!=null)
		{
			sp.release();
			sp=null;
		}
	}

}
