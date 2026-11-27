import json
import unittest
from pathlib import Path


class StreakFlameSpecTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        root = Path(__file__).resolve().parent.parent
        cls.levels = json.loads((root / 'androidApp/src/main/assets/api-level.json').read_text(encoding='utf-8'))['levels']

    def test_seven_thresholds_and_viewbox(self):
        self.assertEqual([level['id'] for level in self.levels], list(range(1, 8)))
        self.assertEqual([level['hari_min'] for level in self.levels], [1, 3, 7, 14, 30, 60, 100])
        self.assertTrue(all(level['viewBox'] == '-10 -40 140 180' for level in self.levels))

    def test_layers_and_baseline(self):
        for level in self.levels:
            for name in ('outer', 'mid', 'inner', 'core', 'highlight'):
                self.assertTrue(level[name]['d'].startswith('M'), (level['id'], name))
            for name in ('outer', 'mid', 'inner', 'core'):
                self.assertIn('136', level[name]['d'], (level['id'], name))
            self.assertGreater(level['glow']['rx'], 0)
            self.assertGreater(level['glow']['ry'], 0)
        self.assertIn('halo', self.levels[-1])
        self.assertTrue(all('halo' not in level for level in self.levels[:-1]))


if __name__ == '__main__':
    unittest.main()
