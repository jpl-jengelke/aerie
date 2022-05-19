import * as AST from './windows-expressions-ast'
import { ActivityType, Resource, transition } from "./mission-model-generated-code";

export class WindowSet {
  public readonly __astnode: AST.WindowsExpression;
  private constructor(windowSpecifer: AST.WindowsExpression) {
    this.__astnode = windowSpecifer;
  }
  private static new(windowSpecifier: AST.WindowsExpression) {
    return new WindowSet(windowSpecifier);
  }

  public static all(...windowSets: WindowSet[]): WindowSet {
    return WindowSet.new({
      kind: AST.NodeKind.WindowsExpressionAnd,
      windowsExpressions: windowSets.map(windowSet => windowSet.__astnode),
    });
  }

  public static any(...windowSets: WindowSet[]): WindowSet {
    return WindowSet.new({
      kind: AST.NodeKind.WindowsExpressionOr,
      windowsExpressions: windowSets.map(windowSet => windowSet.__astnode),
    });
  }

  public static greaterThan(resource: Resource, value: Double): WindowSet {
    return WindowSet.new({
      kind: AST.NodeKind.WindowsExpressionGreaterThan,
      left: resource,
      right: value,
    });
  }

  public static lessThan(resource: Resource, value: Double): WindowSet {
    return WindowSet.new({
      kind: AST.NodeKind.WindowsExpressionLessThan,
      left: resource,
      right: value,
    });
  }

  public static equalTo(resource: Resource, value: Double): WindowSet {
    return WindowSet.new({
      kind: AST.NodeKind.WindowsExpressionEqualLinear,
      left: resource,
      right: value,
    });
  }

  public static notEqualTo(resource: Resource, value: Double): WindowSet {
    return WindowSet.new({
      kind: AST.NodeKind.WindowsExpressionNotEqualLinear,
      left: resource,
      right: value,
    });
  }

  public static transition(resource: Resource, from: any, to: any): WindowSet {
    return WindowSet.new({
      kind: AST.NodeKind.WindowsExpressionTransition,
      resource,
      from,
      to
    });
  }

  public static during(activityType: ActivityType): WindowSet {
    return WindowSet.new({
      kind: AST.NodeKind.ActivityExpression,
      type: activityType
    })
  }
}

declare global {
  class WindowSet {
    public readonly __astnode: AST.WindowsExpression
    public static greaterThan(resource: Resource, value: Double): WindowSet
    public static lessThan(resource: Resource, value: Double): WindowSet
    public static equalTo(resource: Resource, value: Double): WindowSet
    public static notEqualTo(resource: Resource, value: Double): WindowSet
    public static between(resource: Resource, lowerBound: Double, upperBound: Double): WindowSet
    public static transition: typeof transition
    public static during(activityType: ActivityType): WindowSet
    public static all(...windowSets: WindowSet[]): WindowSet
    public static any(...windowSets: WindowSet[]): WindowSet
  }
}

// Make Goal available on the global object
Object.assign(globalThis, { WindowSet });
