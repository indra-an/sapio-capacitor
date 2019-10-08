declare module "@capacitor/core" {
  interface PluginRegistry {
    SapioPlugin: SapioPluginPlugin;
  }
}

export interface SapioPluginPlugin {
  echo(options: { value: string }): Promise<{value: string}>;
}
