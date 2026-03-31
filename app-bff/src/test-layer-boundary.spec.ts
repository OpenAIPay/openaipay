import * as fs from 'node:fs';
import * as path from 'node:path';

describe('BffTestLayerBoundary', () => {
  it('should not keep service or client unit specs under src', () => {
    const rootDir = path.resolve(__dirname);
    const specFiles = walk(rootDir)
      .filter((filePath) => filePath.endsWith('.spec.ts'))
      .map((filePath) => path.relative(rootDir, filePath).replaceAll(path.sep, '/'));

    const allowedServiceSpecs = new Set([
      'auth/auth.service.spec.ts',
      'bill/bill.service.spec.ts',
      'user-flow/user-flow.service.spec.ts',
    ]);
    const forbidden = specFiles.filter(
      (filePath) =>
        (filePath.endsWith('.service.spec.ts') && !allowedServiceSpecs.has(filePath))
        || filePath === 'common/upstream.spec.ts',
    );

    expect(forbidden).toEqual([]);
  });

  it('should keep specs at controller or boundary policy layer', () => {
    const rootDir = path.resolve(__dirname);
    const specFiles = walk(rootDir)
      .filter((filePath) => filePath.endsWith('.spec.ts'))
      .map((filePath) => path.relative(rootDir, filePath).replaceAll(path.sep, '/'));

    const allowedStandaloneSpecs = new Set([
      'bill/bill.service.spec.ts',
      'common/auth-token.spec.ts',
      'common/auth.middleware.spec.ts',
      'common/error.filter.spec.ts',
      'controller-endpoint.smoke.spec.ts',
      'proxy/api-proxy.policy.spec.ts',
      'test-layer-boundary.spec.ts',
      'auth/auth.service.spec.ts',
      'user-flow/user-flow.service.spec.ts',
    ]);

    const forbidden = specFiles.filter(
      (filePath) =>
        !filePath.endsWith('.controller.spec.ts')
        && !filePath.endsWith('.contract.spec.ts')
        && !allowedStandaloneSpecs.has(filePath),
    );

    expect(forbidden).toEqual([]);
  });
});

function walk(directory: string): string[] {
  const entries = fs.readdirSync(directory, { withFileTypes: true });
  const files: string[] = [];
  for (const entry of entries) {
    const absolutePath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      files.push(...walk(absolutePath));
      continue;
    }
    files.push(absolutePath);
  }
  return files;
}
