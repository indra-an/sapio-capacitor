import { WebPlugin } from '@capacitor/core';
import { SapioPluginPlugin } from './definitions';

export class SapioPluginWeb extends WebPlugin implements SapioPluginPlugin {
  constructor() {
    super({
      name: 'SapioPlugin',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const SapioPlugin = new SapioPluginWeb();

export { SapioPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(SapioPlugin);
