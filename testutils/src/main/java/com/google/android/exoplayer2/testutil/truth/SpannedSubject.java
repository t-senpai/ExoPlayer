/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.google.android.exoplayer2.testutil.truth;

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import android.graphics.Typeface;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import java.util.ArrayList;
import java.util.List;

/** A Truth {@link Subject} for assertions on {@link Spanned} instances containing text styling. */
// TODO: add support for more Spans i.e. all those used in com.google.android.exoplayer2.text.
public final class SpannedSubject extends Subject {

  @Nullable private final Spanned actual;

  private SpannedSubject(FailureMetadata metadata, @Nullable Spanned actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public static Factory<SpannedSubject, Spanned> spanned() {
    return SpannedSubject::new;
  }

  /**
   * Convenience method to create a SpannedSubject.
   *
   * <p>Can be statically imported alongside other Truth {@code assertThat} methods.
   *
   * @param spanned The subject under test.
   * @return An object for conducting assertions on the subject.
   */
  public static SpannedSubject assertThat(@Nullable Spanned spanned) {
    return assertAbout(spanned()).that(spanned);
  }

  public void hasNoSpans() {
    Object[] spans = actual.getSpans(0, actual.length(), Object.class);
    if (spans.length > 0) {
      failWithoutActual(
          simpleFact("Expected no spans"),
          fact("in text", actual),
          fact("but found", getAllSpansAsStringWithoutFlags(actual)));
    }
  }

  /**
   * Checks that the subject has an italic span from {@code startIndex} to {@code endIndex}.
   *
   * @param startIndex The start of the expected span.
   * @param endIndex The end of the expected span.
   * @param flags The flags of the expected span. See constants on {@link Spanned} for more
   *     information.
   */
  // TODO: swap this to fluent-style.
  public void hasItalicSpan(int startIndex, int endIndex, int flags) {
    hasStyleSpan(startIndex, endIndex, flags, Typeface.ITALIC);
  }

  /**
   * Checks that the subject has a bold span from {@code startIndex} to {@code endIndex}.
   *
   * @param startIndex The start of the expected span.
   * @param endIndex The end of the expected span.
   * @param flags The flags of the expected span. See constants on {@link Spanned} for more
   *     information.
   */
  // TODO: swap this to fluent-style.
  public void hasBoldSpan(int startIndex, int endIndex, int flags) {
    hasStyleSpan(startIndex, endIndex, flags, Typeface.BOLD);
  }

  private void hasStyleSpan(int startIndex, int endIndex, int flags, int style) {
    if (actual == null) {
      failWithoutActual(simpleFact("Spanned must not be null"));
      return;
    }

    for (StyleSpan span : findMatchingSpans(startIndex, endIndex, flags, StyleSpan.class)) {
      if (span.getStyle() == style) {
        return;
      }
    }

    failWithExpectedSpanWithFlags(
        startIndex,
        endIndex,
        flags,
        new StyleSpan(style),
        actual.toString().substring(startIndex, endIndex));
  }

  /**
   * Checks that the subject has bold and italic styling from {@code startIndex} to {@code
   * endIndex}.
   *
   * <p>This can either be:
   *
   * <ul>
   *   <li>A single {@link StyleSpan} with {@code span.getStyle() == Typeface.BOLD_ITALIC}.
   *   <li>Two {@link StyleSpan}s, one with {@code span.getStyle() == Typeface.BOLD} and the other
   *       with {@code span.getStyle() == Typeface.ITALIC}.
   * </ul>
   *
   * @param startIndex The start of the expected span.
   * @param endIndex The end of the expected span.
   * @param flags The flags of the expected span. See constants on {@link Spanned} for more
   *     information.
   */
  // TODO: swap this to fluent-style.
  public void hasBoldItalicSpan(int startIndex, int endIndex, int flags) {
    if (actual == null) {
      failWithoutActual(simpleFact("Spanned must not be null"));
      return;
    }

    List<Integer> styles = new ArrayList<>();
    for (StyleSpan span : findMatchingSpans(startIndex, endIndex, flags, StyleSpan.class)) {
      styles.add(span.getStyle());
    }
    if (styles.size() == 1 && styles.contains(Typeface.BOLD_ITALIC)) {
      return;
    } else if (styles.size() == 2
        && styles.contains(Typeface.BOLD)
        && styles.contains(Typeface.ITALIC)) {
      return;
    }

    String spannedSubstring = actual.toString().substring(startIndex, endIndex);
    String boldSpan =
        getSpanAsStringWithFlags(
            startIndex, endIndex, flags, new StyleSpan(Typeface.BOLD), spannedSubstring);
    String italicSpan =
        getSpanAsStringWithFlags(
            startIndex, endIndex, flags, new StyleSpan(Typeface.ITALIC), spannedSubstring);
    String boldItalicSpan =
        getSpanAsStringWithFlags(
            startIndex, endIndex, flags, new StyleSpan(Typeface.BOLD_ITALIC), spannedSubstring);

    failWithoutActual(
        simpleFact("No matching span found"),
        fact("in text", actual.toString()),
        fact("expected either", boldItalicSpan),
        fact("or both", boldSpan + "\n" + italicSpan),
        fact("but found", getAllSpansAsStringWithFlags(actual)));
  }

  /**
   * Checks that the subject has an {@link UnderlineSpan} from {@code start} to {@code end}.
   *
   * @param start The start of the expected span.
   * @param end The end of the expected span.
   * @return A {@link WithSpanFlags} object for optional additional assertions on the flags.
   */
  public WithSpanFlags hasUnderlineSpanBetween(int start, int end) {
    if (actual == null) {
      failWithoutActual(simpleFact("Spanned must not be null"));
      return ALREADY_FAILED_WITH_FLAGS;
    }

    List<UnderlineSpan> underlineSpans = findMatchingSpans(start, end, UnderlineSpan.class);
    List<Integer> allFlags = new ArrayList<>();
    for (UnderlineSpan span : underlineSpans) {
      allFlags.add(actual.getSpanFlags(span));
    }
    if (underlineSpans.size() == 1) {
      return check("UnderlineSpan (start=%s,end=%s)", start, end).about(spanFlags()).that(allFlags);
    }
    failWithExpectedSpanWithoutFlags(
        start, end, UnderlineSpan.class, actual.toString().substring(start, end));
    return ALREADY_FAILED_WITH_FLAGS;
  }

  /**
   * Checks that the subject as a {@link ForegroundColorSpan} from {@code start} to {@code end}.
   *
   * <p>The color is asserted in a follow-up method call on the return {@link Colored} object.
   *
   * @param start The start of the expected span.
   * @param end The end of the expected span.
   * @return A {@link Colored} object to assert on the color of the matching spans.
   */
  @CheckResult
  public Colored hasForegroundColorSpanBetween(int start, int end) {
    if (actual == null) {
      failWithoutActual(simpleFact("Spanned must not be null"));
      return ALREADY_FAILED_COLORED;
    }

    List<ForegroundColorSpan> foregroundColorSpans =
        findMatchingSpans(start, end, ForegroundColorSpan.class);
    if (foregroundColorSpans.isEmpty()) {
      failWithExpectedSpanWithoutFlags(
          start, end, ForegroundColorSpan.class, actual.toString().substring(start, end));
      return ALREADY_FAILED_COLORED;
    }
    return check("ForegroundColorSpan (start=%s,end=%s)", start, end)
        .about(foregroundColorSpans(actual))
        .that(foregroundColorSpans);
  }

  /**
   * Checks that the subject as a {@link ForegroundColorSpan} from {@code start} to {@code end}.
   *
   * <p>The color is asserted in a follow-up method call on the return {@link Colored} object.
   *
   * @param start The start of the expected span.
   * @param end The end of the expected span.
   * @return A {@link Colored} object to assert on the color of the matching spans.
   */
  @CheckResult
  public Colored hasBackgroundColorSpanBetween(int start, int end) {
    if (actual == null) {
      failWithoutActual(simpleFact("Spanned must not be null"));
      return ALREADY_FAILED_COLORED;
    }

    List<BackgroundColorSpan> backgroundColorSpans =
        findMatchingSpans(start, end, BackgroundColorSpan.class);
    if (backgroundColorSpans.isEmpty()) {
      failWithExpectedSpanWithoutFlags(
          start, end, BackgroundColorSpan.class, actual.toString().substring(start, end));
      return ALREADY_FAILED_COLORED;
    }
    return check("BackgroundColorSpan (start=%s,end=%s)", start, end)
        .about(backgroundColorSpans(actual))
        .that(backgroundColorSpans);
  }

  private <T> List<T> findMatchingSpans(
      int startIndex, int endIndex, int flags, Class<T> spanClazz) {
    List<T> spans = new ArrayList<>();
    for (T span : actual.getSpans(startIndex, endIndex, spanClazz)) {
      if (actual.getSpanStart(span) == startIndex
          && actual.getSpanEnd(span) == endIndex
          && actual.getSpanFlags(span) == flags) {
        spans.add(span);
      }
    }
    return spans;
  }

  private <T> List<T> findMatchingSpans(int startIndex, int endIndex, Class<T> spanClazz) {
    List<T> spans = new ArrayList<>();
    for (T span : actual.getSpans(startIndex, endIndex, spanClazz)) {
      if (actual.getSpanStart(span) == startIndex && actual.getSpanEnd(span) == endIndex) {
        spans.add(span);
      }
    }
    return spans;
  }

  private void failWithExpectedSpanWithFlags(
      int start, int end, int flags, Object span, String spannedSubstring) {
    failWithoutActual(
        simpleFact("No matching span found"),
        fact("in text", actual),
        fact("expected", getSpanAsStringWithFlags(start, end, flags, span, spannedSubstring)),
        fact("but found", getAllSpansAsStringWithFlags(actual)));
  }

  private void failWithExpectedSpanWithoutFlags(
      int start, int end, Class<?> spanType, String spannedSubstring) {
    failWithoutActual(
        simpleFact("No matching span found"),
        fact("in text", actual),
        fact("expected", getSpanAsStringWithoutFlags(start, end, spanType, spannedSubstring)),
        fact("but found", getAllSpansAsStringWithoutFlags(actual)));
  }

  private static String getAllSpansAsStringWithFlags(Spanned spanned) {
    List<String> actualSpanStrings = new ArrayList<>();
    for (Object span : spanned.getSpans(0, spanned.length(), Object.class)) {
      actualSpanStrings.add(getSpanAsStringWithFlags(span, spanned));
    }
    return TextUtils.join("\n", actualSpanStrings);
  }

  private static String getSpanAsStringWithFlags(Object span, Spanned spanned) {
    int spanStart = spanned.getSpanStart(span);
    int spanEnd = spanned.getSpanEnd(span);
    return getSpanAsStringWithFlags(
        spanStart,
        spanEnd,
        spanned.getSpanFlags(span),
        span,
        spanned.toString().substring(spanStart, spanEnd));
  }

  private static String getSpanAsStringWithFlags(
      int start, int end, int flags, Object span, String spannedSubstring) {
    String suffix;
    if (span instanceof StyleSpan) {
      suffix = "\tstyle=" + ((StyleSpan) span).getStyle();
    } else {
      suffix = "";
    }
    return String.format(
        "start=%s\tend=%s\tflags=%s\ttype=%s\tsubstring='%s'%s",
        start, end, flags, span.getClass().getSimpleName(), spannedSubstring, suffix);
  }

  private static String getAllSpansAsStringWithoutFlags(Spanned spanned) {
    List<String> actualSpanStrings = new ArrayList<>();
    for (Object span : spanned.getSpans(0, spanned.length(), Object.class)) {
      actualSpanStrings.add(getSpanAsStringWithoutFlags(span, spanned));
    }
    return TextUtils.join("\n", actualSpanStrings);
  }

  private static String getSpanAsStringWithoutFlags(Object span, Spanned spanned) {
    int spanStart = spanned.getSpanStart(span);
    int spanEnd = spanned.getSpanEnd(span);
    return getSpanAsStringWithoutFlags(
        spanStart, spanEnd, span.getClass(), spanned.toString().substring(spanStart, spanEnd));
  }

  private static String getSpanAsStringWithoutFlags(
      int start, int end, Class<?> span, String spannedSubstring) {
    return String.format(
        "start=%s\tend=%s\ttype=%s\tsubstring='%s'",
        start, end, span.getSimpleName(), spannedSubstring);
  }

  /**
   * Allows additional assertions to be made on the flags of matching spans.
   *
   * <p>Identical to {@link WithSpanFlags}, but this should be returned from {@code with...()}
   * methods while {@link WithSpanFlags} should be returned from {@code has...()} methods.
   *
   * <p>See Flag constants on {@link Spanned} for possible values.
   */
  public interface AndSpanFlags {

    /**
     * Checks that one of the matched spans has the expected {@code flags}.
     *
     * @param flags The expected flags. See SPAN_* constants on {@link Spanned} for possible values.
     */
    void andFlags(int flags);
  }

  private static final AndSpanFlags ALREADY_FAILED_AND_FLAGS = flags -> {};

  /**
   * Allows additional assertions to be made on the flags of matching spans.
   *
   * <p>Identical to {@link AndSpanFlags}, but this should be returned from {@code has...()} methods
   * while {@link AndSpanFlags} should be returned from {@code with...()} methods.
   */
  public interface WithSpanFlags {

    /**
     * Checks that one of the matched spans has the expected {@code flags}.
     *
     * @param flags The expected flags. See SPAN_* constants on {@link Spanned} for possible values.
     */
    void withFlags(int flags);
  }

  private static final WithSpanFlags ALREADY_FAILED_WITH_FLAGS = flags -> {};

  private static Factory<SpanFlagsSubject, List<Integer>> spanFlags() {
    return SpanFlagsSubject::new;
  }

  private static final class SpanFlagsSubject extends Subject
      implements AndSpanFlags, WithSpanFlags {

    private final List<Integer> flags;

    private SpanFlagsSubject(FailureMetadata metadata, List<Integer> flags) {
      super(metadata, flags);
      this.flags = flags;
    }

    @Override
    public void andFlags(int flags) {
      check("contains()").that(this.flags).contains(flags);
    }

    @Override
    public void withFlags(int flags) {
      andFlags(flags);
    }
  }

  /** Allows assertions about the color of a span. */
  public interface Colored {

    /**
     * Checks that at least one of the matched spans has the expected {@code color}.
     *
     * @param color The expected color.
     * @return A {@link WithSpanFlags} object for optional additional assertions on the flags.
     */
    AndSpanFlags withColor(@ColorInt int color);
  }

  private static final Colored ALREADY_FAILED_COLORED = color -> ALREADY_FAILED_AND_FLAGS;

  private Factory<ForegroundColorSpansSubject, List<ForegroundColorSpan>> foregroundColorSpans(
      Spanned actualSpanned) {
    return (FailureMetadata metadata, List<ForegroundColorSpan> spans) ->
        new ForegroundColorSpansSubject(metadata, spans, actualSpanned);
  }

  private static final class ForegroundColorSpansSubject extends Subject implements Colored {

    private final List<ForegroundColorSpan> actualSpans;
    private final Spanned actualSpanned;

    private ForegroundColorSpansSubject(
        FailureMetadata metadata, List<ForegroundColorSpan> actualSpans, Spanned actualSpanned) {
      super(metadata, actualSpans);
      this.actualSpans = actualSpans;
      this.actualSpanned = actualSpanned;
    }

    @Override
    public AndSpanFlags withColor(@ColorInt int color) {
      List<Integer> matchingSpanFlags = new ArrayList<>();
      // Use hex strings for comparison so the values in error messages are more human readable.
      List<String> spanColors = new ArrayList<>();

      for (ForegroundColorSpan span : actualSpans) {
        spanColors.add(String.format("0x%08X", span.getForegroundColor()));
        if (span.getForegroundColor() == color) {
          matchingSpanFlags.add(actualSpanned.getSpanFlags(span));
        }
      }

      String expectedColorString = String.format("0x%08X", color);
      check("foregroundColor").that(spanColors).containsExactly(expectedColorString);
      return check("flags").about(spanFlags()).that(matchingSpanFlags);
    }
  }

  private Factory<BackgroundColorSpansSubject, List<BackgroundColorSpan>> backgroundColorSpans(
      Spanned actualSpanned) {
    return (FailureMetadata metadata, List<BackgroundColorSpan> spans) ->
        new BackgroundColorSpansSubject(metadata, spans, actualSpanned);
  }

  private static final class BackgroundColorSpansSubject extends Subject implements Colored {

    private final List<BackgroundColorSpan> actualSpans;
    private final Spanned actualSpanned;

    private BackgroundColorSpansSubject(
        FailureMetadata metadata, List<BackgroundColorSpan> actualSpans, Spanned actualSpanned) {
      super(metadata, actualSpans);
      this.actualSpans = actualSpans;
      this.actualSpanned = actualSpanned;
    }

    @Override
    public AndSpanFlags withColor(@ColorInt int color) {
      List<Integer> matchingSpanFlags = new ArrayList<>();
      // Use hex strings for comparison so the values in error messages are more human readable.
      List<String> spanColors = new ArrayList<>();

      for (BackgroundColorSpan span : actualSpans) {
        spanColors.add(String.format("0x%08X", span.getBackgroundColor()));
        if (span.getBackgroundColor() == color) {
          matchingSpanFlags.add(actualSpanned.getSpanFlags(span));
        }
      }

      String expectedColorString = String.format("0x%08X", color);
      check("backgroundColor").that(spanColors).containsExactly(expectedColorString);
      return check("flags").about(spanFlags()).that(matchingSpanFlags);
    }
  }
}