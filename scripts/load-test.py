#!/usr/bin/env python3
"""
Emirates Digital Bank — Performance Load Test
Tests 500+ parallel login attempts against Keycloak OAuth2 token endpoint
"""

import asyncio
import aiohttp
import time
import sys
import statistics
from dataclasses import dataclass, field
from typing import List

KEYCLOAK_TOKEN_URL = "http://localhost:9080/realms/dbf/protocol/openid-connect/token"
CLIENT_ID = "dbf-web"
USERNAME = "loadtest"
PASSWORD = "Test1234!"
TOTAL_REQUESTS = 500
CONCURRENCY = 500  # All 500 at once

@dataclass
class LoadTestResult:
    total_requests: int = 0
    successful: int = 0
    failed: int = 0
    errors: dict = field(default_factory=dict)
    latencies: List[float] = field(default_factory=list)
    start_time: float = 0
    end_time: float = 0

    @property
    def duration(self):
        return self.end_time - self.start_time

    @property
    def rps(self):
        return self.total_requests / self.duration if self.duration > 0 else 0

    @property
    def success_rate(self):
        return (self.successful / self.total_requests * 100) if self.total_requests > 0 else 0

    @property
    def p50(self):
        return self._percentile(50)

    @property
    def p95(self):
        return self._percentile(95)

    @property
    def p99(self):
        return self._percentile(99)

    def _percentile(self, p):
        if not self.latencies:
            return 0
        sorted_lat = sorted(self.latencies)
        idx = int(len(sorted_lat) * p / 100)
        return sorted_lat[min(idx, len(sorted_lat) - 1)]


async def login_request(session, semaphore, result):
    async with semaphore:
        start = time.monotonic()
        try:
            data = {
                "grant_type": "password",
                "client_id": CLIENT_ID,
                "username": USERNAME,
                "password": PASSWORD,
            }
            async with session.post(KEYCLOAK_TOKEN_URL, data=data) as resp:
                latency = time.monotonic() - start
                result.latencies.append(latency)
                result.total_requests += 1

                if resp.status == 200:
                    body = await resp.json()
                    if "access_token" in body:
                        result.successful += 1
                    else:
                        result.failed += 1
                        err = body.get("error", "no_token")
                        result.errors[err] = result.errors.get(err, 0) + 1
                else:
                    result.failed += 1
                    err = f"HTTP_{resp.status}"
                    result.errors[err] = result.errors.get(err, 0) + 1
        except Exception as e:
            latency = time.monotonic() - start
            result.latencies.append(latency)
            result.total_requests += 1
            result.failed += 1
            err = type(e).__name__
            result.errors[err] = result.errors.get(err, 0) + 1


def print_banner():
    print("=" * 70)
    print("  Emirates Digital Bank — Performance Load Test")
    print("  OAuth2 Token Endpoint Stress Test")
    print("=" * 70)
    print(f"  Target:      {KEYCLOAK_TOKEN_URL}")
    print(f"  Requests:    {TOTAL_REQUESTS}")
    print(f"  Concurrency: {CONCURRENCY}")
    print(f"  Client:      {CLIENT_ID}")
    print("=" * 70)


def print_results(result):
    print("\n" + "=" * 70)
    print("  LOAD TEST RESULTS")
    print("=" * 70)
    print(f"  Total Requests:     {result.total_requests}")
    print(f"  Successful:         {result.successful}")
    print(f"  Failed:             {result.failed}")
    print(f"  Success Rate:       {result.success_rate:.1f}%")
    print(f"  Total Duration:     {result.duration:.2f}s")
    print(f"  Requests/sec:       {result.rps:.1f}")
    print("-" * 70)
    if result.latencies:
        print(f"  Avg Latency:        {statistics.mean(result.latencies)*1000:.0f}ms")
        print(f"  Min Latency:        {min(result.latencies)*1000:.0f}ms")
        print(f"  Max Latency:        {max(result.latencies)*1000:.0f}ms")
        print(f"  P50 Latency:        {result.p50*1000:.0f}ms")
        print(f"  P95 Latency:        {result.p95*1000:.0f}ms")
        print(f"  P99 Latency:        {result.p99*1000:.0f}ms")
        print(f"  Std Dev:            {statistics.stdev(result.latencies)*1000:.0f}ms" if len(result.latencies) > 1 else "")
    if result.errors:
        print("-" * 70)
        print("  Errors:")
        for err, count in sorted(result.errors.items(), key=lambda x: -x[1]):
            print(f"    {err}: {count}")
    print("=" * 70)

    if result.success_rate >= 95:
        print("  VERDICT: PASS — System handles 500+ concurrent logins")
    elif result.success_rate >= 80:
        print("  VERDICT: ACCEPTABLE — Some degradation under load")
    else:
        print("  VERDICT: FAIL — Significant failures under load")
    print("=" * 70)


async def run_warmup(session):
    print("\n  Warm-up: 10 sequential requests...")
    for i in range(10):
        data = {
            "grant_type": "password",
            "client_id": CLIENT_ID,
            "username": USERNAME,
            "password": PASSWORD,
        }
        try:
            async with session.post(KEYCLOAK_TOKEN_URL, data=data) as resp:
                await resp.json()
                print(f"    Warm-up {i+1}/10: HTTP {resp.status}")
        except Exception as e:
            print(f"    Warm-up {i+1}/10: ERROR {e}")


async def main():
    print_banner()

    connector = aiohttp.TCPConnector(
        limit=0,
        ttl_dns_cache=300,
        force_close=False,
    )
    timeout = aiohttp.ClientTimeout(total=60)

    async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
        await run_warmup(session)

        result = LoadTestResult()
        semaphore = asyncio.Semaphore(CONCURRENCY)

        print(f"\n  Launching {TOTAL_REQUESTS} concurrent login requests...")
        result.start_time = time.monotonic()

        tasks = [login_request(session, semaphore, result) for _ in range(TOTAL_REQUESTS)]
        await asyncio.gather(*tasks)

        result.end_time = time.monotonic()

    print_results(result)
    return result


if __name__ == "__main__":
    result = asyncio.run(main())
    sys.exit(0 if result.success_rate >= 80 else 1)
