/**
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @class PolySynthVoice
 * @description A voice generates a waveform and filters it, exposing the
 *              sound through |this.output|.
 */
 /**
   * @constructor
   * @param {AudioContext} context The audio context.
   * @param {String} noteName The name of the note corresponding to the pitch.
   * @param {Number} frequency The corresponding frequency of the note, e.g 440.
   * @param {PolySynth} synth The synthesizer that manages this voice.
   */
function PolySynthVoice(context, noteName, frequency, synth) {
    this.synth_ = synth;
    this.context_ = context;
    this.parameters_ = synth.getParameters();

    // The name of the note is used as an argument in the
    // |this.synth_.endNote()| callback.
    this.noteName_ = noteName;
    this.frequency_ = frequency;

    this.oscillatorA_ = new OscillatorNode(
        this.context_, {frequency: frequency, type: 'sawtooth'});
    this.lowPassFilter_ = new BiquadFilterNode(this.context_, {
      frequency: this.parameters_.filterCutoff,
      type: 'lowpass',
      Q: this.parameters_.filterQ
    });

    this.output = new GainNode(this.context_);
    // this.oscillatorA_.connect(this.lowPassFilter_).connect(this.output);
    this.oscillatorA_.connect(this.output);
    this.oscillatorA_.start();

    // The synthesizer should remove its reference to this voice once the
    // oscillator has stopped.
    this.oscillatorA_.onended = this.synth_.endVoice(this.noteName_);
 
  
  /* Change to specified frequency in time seconds */
  this.setFrequency = function(frequency, time) {
    this.oscillatorA_.frequency.linearRampToValueAtTime(frequency, time);
  }
  
    /**
   * Play a note according to ADSR settings.
   */
  this.start = function() {
    // Ramp to full amplitude in attack (s) and to sustain in decay (s).
    var t = this.context_.currentTime;
    var timeToFullAmplitude = t + this.parameters_.gainAttack;
    var timeToGainSustain =
        timeToFullAmplitude + this.parameters_.gainDecay;

    this.output.gain.setValueAtTime(0, t);
    this.output.gain.linearRampToValueAtTime(1, timeToFullAmplitude);
    this.output.gain.linearRampToValueAtTime(
        this.parameters_.gainSustain, timeToGainSustain);

    // The detune of the filter reaches its peak amount specified by
    // |filterDetuneAmount| (where 1 corresponds to 2400 cents detuning) in
    // |filterAttack| seconds. It then decays to a fraction of that amount as
    // specified by |filterSustain| in |filterDecay| seconds.
    var standardFilterDetuneInCents = 2400;
    var amountOfPeakDetuneInCents =
        standardFilterDetuneInCents * this.parameters_.filterDetuneAmount;
    var amountOfSustainDetuneInCents =
        amountOfPeakDetuneInCents * this.parameters_.filterSustain;

    var timeToFullDetune = t + this.parameters_.filterAttack;
    var timeToDetuneSustain
        = timeToFullDetune + this.parameters_.filterDecay;

    this.lowPassFilter_.detune.linearRampToValueAtTime(
        amountOfSustainDetuneInCents, timeToDetuneSustain);
    this.lowPassFilter_.detune.linearRampToValueAtTime(
        amountOfPeakDetuneInCents, timeToFullDetune);
  }

  /**
   * On key release, stop the note according to |this.release_|.
   */
  this.release = function() {
    // Cancel scheduled audio param changes, and fade note according to
    // release time.
    var t = this.context_.currentTime;
    var timeToZeroAmplitude = t + this.parameters_.gainRelease;
    this.output.gain.cancelAndHoldAtTime(t);
    this.output.gain.linearRampToValueAtTime(0, timeToZeroAmplitude);

    // Fade detune for filter to 0 according to release time.
    var timeToZeroDetune = t + this.parameters_.filterRelease;
    this.lowPassFilter_.detune.cancelAndHoldAtTime(t);
    this.lowPassFilter_.detune.linearRampToValueAtTime(
        0, timeToZeroDetune);

    this.oscillatorA_.stop(timeToZeroAmplitude);
  }
}
