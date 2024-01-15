// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/** @type {import('@playwright/test').PlaywrightTestConfig} */
import dotenv from 'dotenv';

dotenv.config();
const config = {
	webServer: {
		command: 'npm run start:frontend',
		url: 'http://127.0.0.1:3000',
    reuseExistingServer: false
	},
  retries: 2,
  timeout: 30000,
	testDir: './tests',
	reporter: [ ['html', { open: 'never' }] ],
  use: {
    video: {
      mode: 'retain-on-failure',
      size: { width: 640, height: 480 },
      dir: "frontend/playwright-report"
    }
  }
};

export default config;
