package me.roundaround.inventorymanagement.config;

import me.roundaround.trove.config.ConfigPath;
import me.roundaround.trove.config.option.ConfigOption;

import java.util.*;

public class IntListConfigOption extends ConfigOption<List<Integer>> {
  protected IntListConfigOption(Builder builder) {
    super(builder);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void deserialize(Object data) {
    this.setValue(((List<Object>) data).stream()
        .map((value) -> value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value)))
        .toList());
  }

  @Override
  public void setValue(List<Integer> pendingValue) {
    // Copy on set so the caller can't then modify their local copy and corrupt the config
    super.setValue(List.copyOf(pendingValue));
  }

  @Override
  protected boolean areValuesEqual(List<Integer> a, List<Integer> b) {
    if ((a == null) != (b == null)) {
      return false;
    }

    if (a == null) {
      return true;
    }

    if (a.size() != b.size()) {
      return false;
    }

    Iterator<Integer> iterA = a.iterator();
    Iterator<Integer> iterB = b.iterator();
    while (iterA.hasNext() && iterB.hasNext()) {
      if (!Objects.equals(iterA.next(), iterB.next())) {
        return false;
      }
    }
    return true;
  }

  public void add(int entry) {
    List<Integer> copy = new ArrayList<>(super.getValue());
    copy.add(entry);
    this.setValue(copy);
  }

  public void remove(int entry) {
    List<Integer> copy = new ArrayList<>(super.getValue());
    copy.remove(Integer.valueOf(entry));
    this.setValue(copy);
  }

  public static Builder builder(ConfigPath path) {
    return new Builder(path);
  }

  public static class Builder extends AbstractBuilder<List<Integer>, IntListConfigOption, Builder> {
    private Builder(ConfigPath path) {
      super(path);

      this.setDefaultValue(List.of());
    }

    @Override
    protected IntListConfigOption buildInternal() {
      return new IntListConfigOption(this);
    }
  }
}
